package io.extremum.everything.services.management;

import io.extremum.common.collection.CollectionCoordinates;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.OwnedCoordinates;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.models.BasicModel;
import io.extremum.common.models.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.CollectionFetcher;
import io.extremum.everything.services.collection.CoordinatesHandler;
import io.extremum.everything.services.collection.FetchByOwnedCoordinates;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DefaultEverythingCollectionService implements EverythingCollectionService {
    private final ModelRetriever modelRetriever;
    private final List<CollectionFetcher> collectionFetchers;
    private final DtoConversionService dtoConversionService;
    private final UniversalDao universalDao;

    @Override
    public CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id,
            Projection projection, boolean expand) {
        CoordinatesHandler coordinatesHandler = findCoordinatesHandler(id.getType());
        CollectionFragment<Model> fragment = coordinatesHandler.fetchCollection(id.getCoordinates(), projection);
        return fragment.map(model -> convertModelToResponseDto(model, expand));
    }

    @Override
    public Flux<ResponseDto> streamCollection(CollectionDescriptor id, Projection projection, boolean expand) {
        CoordinatesHandler coordinatesHandler = findCoordinatesHandler(id.getType());
        Flux<Model> models = coordinatesHandler.streamCollection(id.getCoordinates(), projection);
        return models.map(model -> convertModelToResponseDto(model, expand));
    }

    private CoordinatesHandler findCoordinatesHandler(CollectionDescriptor.Type type) {
        if (type == CollectionDescriptor.Type.OWNED) {
            return new OwnedCoordinatesHandler();
        }

        throw new IllegalStateException("Unsupported type: " + type);
    }

    private ResponseDto convertModelToResponseDto(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
    }

    private class OwnedCoordinatesHandler implements CoordinatesHandler {
        @Override
        public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
            OwnedCoordinates owned = coordinates.getOwnedCoordinates();
            BasicModel host = retrieveHost(owned);

            Optional<CollectionFetcher> optFetcher = collectionFetchers.stream()
                    .filter(fetcher -> fetcher.getSupportedModel().equals(owned.getHostId().getModelType()))
                    .filter(fetcher -> fetcher.getHostAttributeName().equals(owned.getHostAttributeName()))
                    .findFirst();

            @SuppressWarnings("unchecked")
            CollectionFragment<Model> castResult = optFetcher
                    .map(fetcher -> fetcher.fetchCollection(host, projection))
                    .orElseGet(() -> fetchUsingDefaultConvention(owned, host, projection));
            return castResult;
        }

        private BasicModel retrieveHost(OwnedCoordinates owned) {
            Model host = modelRetriever.retrieveModel(owned.getHostId());
            if (host == null) {
                throw createHostNotFoundException(owned);
            }

            return castToBasicModel(owned, host);
        }

        private EverythingEverythingException createHostNotFoundException(OwnedCoordinates owned) {
            String message = format("No host entity was found by external ID '%s'",
                    owned.getHostId().getExternalId());
            return new EverythingEverythingException(message);
        }

        private BasicModel castToBasicModel(OwnedCoordinates owned, Model host) {
            if (!(host instanceof BasicModel)) {
                throw new EverythingEverythingException(String.format("Host '%s' is not a BasicModel",
                        owned.getHostId().getModelType()));
            }

            return (BasicModel) host;
        }

        private CollectionFragment<Model> fetchUsingDefaultConvention(OwnedCoordinates owned,
                BasicModel host, Projection projection) {
            FetchByOwnedCoordinates fetcher = new FetchByOwnedCoordinates(universalDao);
            return fetcher.fetchCollection(host, owned.getHostAttributeName(), projection);
        }

        @Override
        public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
            OwnedCoordinates owned = coordinates.getOwnedCoordinates();
            Mono<BasicModel> host = retrieveHostReactively(owned);

//            Optional<CollectionFetcher> optFetcher = collectionFetchers.stream()
//                    .filter(fetcher -> fetcher.getSupportedModel().equals(owned.getHostId().getModelType()))
//                    .filter(fetcher -> fetcher.getHostAttributeName().equals(owned.getHostAttributeName()))
//                    .findFirst();
//
//            @SuppressWarnings("unchecked")
//            CollectionFragment<Model> castResult = optFetcher
//                    .map(fetcher -> fetcher.fetchCollection(host, projection))
//                    .orElseGet(() -> fetchUsingDefaultConvention(owned, host, projection));
//            return castResult;

            return fetchUsingDefaultConventionReactively(owned, host, projection);
        }

        private Mono<BasicModel> retrieveHostReactively(OwnedCoordinates coordinates) {
            return modelRetriever.retrieveModelReactively(coordinates.getHostId())
                    .switchIfEmpty(Mono.error(() -> createHostNotFoundException(coordinates)))
                    .map(host -> castToBasicModel(coordinates, host));
        }

        private Flux<Model> fetchUsingDefaultConventionReactively(OwnedCoordinates owned,
                                                                  Mono<BasicModel> hostProducer,
                                                                  Projection projection) {
            // TODO: use real reactivity!
            return hostProducer.map(host -> fetchUsingDefaultConvention(owned, host, projection))
                    .flatMapIterable(CollectionFragment::elements);
        }
    }
}
