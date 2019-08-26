package io.extremum.everything.services.management;

import io.extremum.common.collection.CollectionCoordinates;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.OwnedCoordinates;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.models.BasicModel;
import io.extremum.common.models.Model;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.CollectionFetcher;
import io.extremum.everything.services.CollectionStreamer;
import io.extremum.everything.services.collection.CoordinatesHandler;
import io.extremum.everything.services.collection.FetchByOwnedCoordinates;
import io.extremum.everything.services.collection.StreamByOwnedCoordinates;
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
    private final List<CollectionStreamer> collectionStreamers;
    private final DtoConversionService dtoConversionService;
    private final UniversalDao universalDao;
    private final Reactifier reactifier;
    private final CollectionTransactivity transactivity;

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
        return models.flatMap(model -> convertModelToResponseDtoReactively(model, expand));
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

    private Mono<ResponseDto> convertModelToResponseDtoReactively(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDtoReactively(model, conversionConfig);
    }

    private class OwnedCoordinatesHandler implements CoordinatesHandler {
        @Override
        public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
            OwnedCoordinates owned = coordinates.getOwnedCoordinates();
            BasicModel host = retrieveHost(owned);

            Optional<CollectionFetcher> optFetcher = findFetcher(owned);

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

        private Optional<CollectionFetcher> findFetcher(OwnedCoordinates owned) {
            return collectionFetchers.stream()
                            .filter(fetcher -> fetcher.getSupportedModel().equals(owned.getHostId().getModelType()))
                            .filter(fetcher -> fetcher.getHostAttributeName().equals(owned.getHostAttributeName()))
                            .findFirst();
        }

        private CollectionFragment<Model> fetchUsingDefaultConvention(OwnedCoordinates owned,
                BasicModel host, Projection projection) {
            FetchByOwnedCoordinates fetcher = new FetchByOwnedCoordinates(universalDao);
            return fetcher.fetch(host, owned.getHostAttributeName(), projection);
        }

        @Override
        public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
            OwnedCoordinates owned = coordinates.getOwnedCoordinates();
            if (dbAccessShouldBeMadeInATransaction(owned)) {
                return streamBlockingFetchResult(coordinates, projection, owned);
            }

            return streamReactively(projection, owned);
        }

        private boolean dbAccessShouldBeMadeInATransaction(OwnedCoordinates owned) {
            return transactivity.isCollectionTransactional(owned.getHostId());
        }

        private Flux<Model> streamBlockingFetchResult(CollectionCoordinates coordinates, Projection projection,
                                                      OwnedCoordinates owned) {
            return reactifier.flux(() -> {
                return transactivity.doInTransaction(owned.getHostId(), () -> {
                    return fetchCollection(coordinates, projection).elements();
                });
            });
        }

        private Flux<Model> streamReactively(Projection projection, OwnedCoordinates owned) {
            Mono<BasicModel> hostMono = retrieveHostReactively(owned);

            Optional<CollectionStreamer> optStreamer = findStreamer(owned);

            if (optStreamer.isPresent()) {
                CollectionStreamer streamer = optStreamer.get();
                @SuppressWarnings("unchecked")
                Flux<Model> castModels = hostMono.flatMapMany(host -> streamer.streamCollection(host, projection));
                return castModels;
            } else {
                return fetchUsingDefaultConventionReactively(owned, hostMono, projection);
            }
        }

        private Mono<BasicModel> retrieveHostReactively(OwnedCoordinates coordinates) {
            return modelRetriever.retrieveModelReactively(coordinates.getHostId())
                    .switchIfEmpty(Mono.error(() -> createHostNotFoundException(coordinates)))
                    .map(host -> castToBasicModel(coordinates, host));
        }

        private Optional<CollectionStreamer> findStreamer(OwnedCoordinates owned) {
            return collectionStreamers.stream()
                    .filter(streamer -> streamer.getSupportedModel().equals(owned.getHostId().getModelType()))
                    .filter(streamer -> streamer.getHostAttributeName().equals(owned.getHostAttributeName()))
                    .findFirst();
        }

        private Flux<Model> fetchUsingDefaultConventionReactively(OwnedCoordinates owned,
                                                                  Mono<BasicModel> hostMono,
                                                                  Projection projection) {
            StreamByOwnedCoordinates streamer = new StreamByOwnedCoordinates(universalDao);
            return hostMono.flatMapMany(host ->
                    streamer.stream(host, owned.getHostAttributeName(), projection));
        }
    }
}
