package io.extremum.everything.services.collection;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultEverythingCollectionService implements EverythingCollectionService {
    private final DtoConversionService dtoConversionService;

    private final OwnedCoordinatesHandler ownedCoordinatesHandler;
    private final FreeCoordinatesHandler freeCoordinatesHandler;

    public DefaultEverythingCollectionService(ModelRetriever modelRetriever,
                                              CollectionProviders collectionProviders,
                                              DtoConversionService dtoConversionService, UniversalDao universalDao,
                                              Reactifier reactifier, CollectionTransactivity transactivity) {
        this.dtoConversionService = dtoConversionService;

        ownedCoordinatesHandler = new OwnedCoordinatesHandler(modelRetriever, universalDao,
                collectionProviders, reactifier, transactivity);
        freeCoordinatesHandler = new FreeCoordinatesHandler(collectionProviders);
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

}
