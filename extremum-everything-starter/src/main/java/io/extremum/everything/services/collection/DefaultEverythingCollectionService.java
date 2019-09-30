package io.extremum.everything.services.collection;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.FreeCollectionStreamer;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.OwnedCollectionStreamer;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.FreeCoordinates;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DefaultEverythingCollectionService implements EverythingCollectionService {
    private final DtoConversionService dtoConversionService;

    private final OwnedCoordinatesHandler ownedCoordinatesHandler;
    private final FreeCoordinatesHandler freeCoordinatesHandler;

    public DefaultEverythingCollectionService(ModelRetriever modelRetriever,
                                              List<OwnedCollectionFetcher> ownedCollectionFetchers,
                                              List<OwnedCollectionStreamer> ownedCollectionStreamers,
                                              List<FreeCollectionStreamer<? extends Model>> freeCollectionStreamers,
                                              DtoConversionService dtoConversionService, UniversalDao universalDao,
                                              Reactifier reactifier, CollectionTransactivity transactivity) {
        this.dtoConversionService = dtoConversionService;

        ownedCoordinatesHandler = new OwnedCoordinatesHandler(modelRetriever, universalDao,
                ownedCollectionFetchers, ownedCollectionStreamers, reactifier, transactivity);
        freeCoordinatesHandler = new FreeCoordinatesHandler(freeCollectionStreamers);
    }

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
            return ownedCoordinatesHandler;
        }
        if (type == CollectionDescriptor.Type.FREE) {
            return freeCoordinatesHandler;
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

    @RequiredArgsConstructor
    private static class FreeCoordinatesHandler implements CoordinatesHandler {
        private final List<FreeCollectionStreamer<? extends Model>> freeCollectionStreamers;

        @Override
        public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
            FreeCoordinates freeCoordinates = coordinates.getFreeCoordinates();
            FreeCollectionStreamer<? extends Model> collectionStreamer = findFreeStreamer(freeCoordinates);
            return collectionStreamer.streamCollection(freeCoordinates.getParametersString(), projection)
                    .map(Function.identity());
        }

        private FreeCollectionStreamer<? extends Model> findFreeStreamer(FreeCoordinates freeCoordinates) {
            String freeCollectionName = freeCoordinates.getName();
            return freeCollectionStreamers.stream()
                    .filter(streamer -> streamerSupportsName(streamer, freeCollectionName))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("Did not find a free collection streamer supporting name '%s'",
                                    freeCollectionName)));
        }

        private boolean streamerSupportsName(FreeCollectionStreamer<?> streamer, String freeCollectionName) {
            return Objects.equals(streamer.getCollectionName(), freeCollectionName);
        }
    }
}
