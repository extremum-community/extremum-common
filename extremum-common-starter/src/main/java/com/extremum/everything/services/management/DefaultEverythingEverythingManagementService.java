package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionCoordinates;
import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.OwnedCoordinates;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.services.*;
import com.extremum.everything.services.collection.CoordinatesHandler;
import com.extremum.everything.services.collection.FetchByOwnedCoordinates;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
public class DefaultEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEverythingEverythingManagementService.class);

    private final List<GetterService<? extends Model>> getterServices;
    private final List<PatcherService<? extends Model>> patcherServices;
    private final List<RemovalService> removalServices;
    private final List<CollectionFetcher> collectionFetchers;
    private final DtoConversionService dtoConversionService;
    private final UniversalDao universalDao;

    private ResponseDto convertModelToResponseDto(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
    }

    private Model retrieveModelObject(Descriptor id) {
        String modelName = determineModelName(id);
        GetterService getterService = findGetterService(modelName);
        Model model = getterService.get(id.getInternalId());

        if (model != null) {
            LOGGER.debug(format("Model with ID '%s' was found by service '%s': '%s'", id, getterService, model));
        } else {
            LOGGER.debug(format("Model with ID '%s' wasn't found by service '%s'", id, getterService));
        }
        return model;
    }

    private String determineModelName(Descriptor id) {
        requireNonNull(id, "ID can't be null");

        String modelName = determineModelNameById(id);
        if (modelName == null) {
            LOGGER.error("Unable to determine a model name for id {}", id);
            throw new EverythingEverythingException(format("Can't determine a model name for the ID '%s'", id));
        } else {
            LOGGER.debug("Model name for id {} is {}", id, modelName);
            return modelName;
        }
    }

    private GetterService findGetterService(String modelName) {
        return findServiceForModelOrElseThrow(modelName, GetterService.class, getterServices,
                    () -> new EverythingEverythingException(
                            format("Not a single service of %d supports getting models with name '%s'",
                                    getterServices.size(), modelName))
        );
    }

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        Model model = retrieveModelObject(id);

        if (model != null) {
            return convertModelToResponseDto(model, expand);
        } else {
            return null;
        }
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        String modelName = determineModelName(id);

        PatcherService patcherService = findPatcherService(modelName);

        Model patched = patcherService.patch(id.getInternalId(), patch);
        return convertModelToResponseDto(patched, expand);
    }

    private PatcherService findPatcherService(String modelName) {
        return findServiceForModelOrElseThrow(modelName, PatcherService.class, patcherServices,
                    () -> new EverythingEverythingException(
                            format("Not a single service of %d supports patching models with name '%s'",
                                    patcherServices.size(), modelName))
        );
    }

    @Override
    public void remove(Descriptor id) {
        String modelName = determineModelName(id);

        RemovalService removalService = findRemovalService(modelName);

        removalService.remove(id.getInternalId());
        LOGGER.debug(format("Model with ID '%s' was removed by service '%s'", id, removalService));
    }

    private RemovalService findRemovalService(String modelName) {
        return findServiceForModelOrElseThrow(modelName, RemovalService.class, removalServices,
                    () -> new EverythingEverythingException(
                            format("Not a single service of %d supports removing models with name '%s'",
                                    removalServices.size(), modelName))
        );
    }

    private String determineModelNameById(Descriptor id) {
        requireNonNull(id, "ID can't be null");
        return id.getModelType();
    }

    private <T extends EverythingEverythingService> T findServiceForModel(String modelName,
                                                                          Collection<? extends EverythingEverythingService> services,
                                                                          Class<T> expectedServiceType) {
        requireNonNull(modelName, "Name of a model can't be null");
        requireNonNull(services, "Services list can't be null");

        return services.stream()
                .filter(getIsServiceSupportsModelFilter(modelName))
                .findAny()
                .filter(expectedServiceType::isInstance)
                .map(expectedServiceType::cast)
                .orElse(null);
    }

    private <T extends EverythingEverythingService> T findServiceForModelOrElseThrow(String modelName,
            Class<T> expectedServiceType, Collection<? extends EverythingEverythingService> services,
            Supplier<? extends RuntimeException> exceptionSupplier) {
        T result = findServiceForModel(modelName, services, expectedServiceType);

        if (result == null) {
            LOGGER.error("No services for a {} model support found. {} services were consulted",
                    modelName, services.size());
            throw exceptionSupplier.get();
        } else {
            return result;
        }
    }

    private Predicate<? super EverythingEverythingService> getIsServiceSupportsModelFilter(String modelName) {
        Class<? extends Model> modelClass = ModelClasses.getClassByModelName(modelName);
        return service -> service.isSupportedModel(modelClass) || modelName.equalsIgnoreCase(service.getSupportedModel());
    }


    @Override
    public CollectionFragment<ResponseDto> fetchCollection(CollectionDescriptor id,
            Projection projection, boolean expand) {
        CoordinatesHandler coordinatesHandler = findCoordinatesHandler(id.getType());
        CollectionFragment<Model> fragment = coordinatesHandler.fetchCollection(id.getCoordinates(), projection);
        return fragment.map(model -> convertModelToResponseDto(model, expand));
    }

    private CoordinatesHandler findCoordinatesHandler(CollectionDescriptor.Type type) {
        if (type == CollectionDescriptor.Type.OWNED) {
            return new OwnedCoordinatesHandler();
        }

        throw new IllegalStateException("Unsupported type: " + type);
    }

    private class OwnedCoordinatesHandler implements CoordinatesHandler {
        @Override
        public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
            OwnedCoordinates owned = coordinates.getOwnedCoordinates();
            BasicModel host = retrieveHost(owned);

            Optional<CollectionFetcher> collectionFetcher = collectionFetchers.stream()
                    .filter(fetcher -> fetcher.getSupportedModel().equals(owned.getHostId().getModelType()))
                    .filter(fetcher -> fetcher.getHostAttributeName().equals(owned.getHostAttributeName()))
                    .findFirst();

            return collectionFetcher.map(fetcher -> fetcher.fetchCollection(host, projection))
                    .orElseGet(() -> fetchUsingDefaultConvention(owned, host, projection));
        }

        private BasicModel retrieveHost(OwnedCoordinates owned) {
            Model host = retrieveModelObject(owned.getHostId());
            if (host == null) {
                String message = format("No host entity was found by external ID '%s'",
                        owned.getHostId().getExternalId());
                throw new EverythingEverythingException(message);
            }

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
    }
}
