package com.extremum.everything.services.management;

import com.extremum.common.collection.CollectionCoordinates;
import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.OwnedCoordinates;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.security.EverythingDataSecurity;
import com.extremum.everything.services.*;
import com.extremum.everything.services.collection.CoordinatesHandler;
import com.extremum.everything.services.collection.FetchByOwnedCoordinates;
import com.extremum.everything.services.defaultservices.DefaultGetter;
import com.extremum.everything.services.defaultservices.DefaultPatcher;
import com.extremum.everything.services.defaultservices.DefaultRemover;
import com.extremum.everything.services.defaultservices.DefaultSaver;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public class DefaultEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEverythingEverythingManagementService.class);

    private final ModelRetriever modelRetriever;
    private final List<PatcherService<? extends Model>> patcherServices;
    private final List<SaverService<? extends Model>> saverServices;
    private final List<RemovalService> removalServices;
    private final DefaultPatcher<? extends Model> defaultPatcher;
    private final DefaultSaver<? extends Model> defaultSaver;
    private final DefaultRemover defaultRemover;
    private final List<CollectionFetcher> collectionFetchers;
    private final DtoConversionService dtoConversionService;
    private final UniversalDao universalDao;
    private final EverythingDataSecurity dataSecurity;

    private final ModelNames modelNames = new ModelNames();

    public DefaultEverythingEverythingManagementService(
            List<GetterService<? extends Model>> getterServices,
            List<PatcherService<? extends Model>> patcherServices,
            List<SaverService<? extends Model>> saverServices,
            List<RemovalService> removalServices,
            DefaultGetter<? extends Model> defaultGetter,
            DefaultPatcher<? extends Model> defaultPatcher,
            DefaultSaver<? extends Model> defaultSaver,
            DefaultRemover defaultRemover,
            List<CollectionFetcher> collectionFetchers,
            DtoConversionService dtoConversionService, UniversalDao universalDao,
            EverythingDataSecurity dataSecurity) {
        this.patcherServices = patcherServices;
        this.saverServices = saverServices;
        this.removalServices = removalServices;
        this.defaultPatcher = defaultPatcher;
        this.defaultSaver = defaultSaver;
        this.defaultRemover = defaultRemover;
        this.collectionFetchers = collectionFetchers;
        this.dtoConversionService = dtoConversionService;
        this.universalDao = universalDao;
        this.dataSecurity = dataSecurity;

        modelRetriever = new ModelRetriever(getterServices, defaultGetter);
    }

    private ResponseDto convertModelToResponseDto(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
    }

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        Model model = modelRetriever.retrieveModel(id);

        dataSecurity.checkGetAllowed(model);

        if (model == null) {
            throw new ModelNotFoundException(String.format("Nothing was found by '%s'", id.getExternalId()));
        }

        return convertModelToResponseDto(model, expand);
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        String modelName = modelNames.determineModelName(id);

        Patcher patcher = findPatcher(modelName);

        Model patched = patcher.patch(id.getInternalId(), patch);
        return convertModelToResponseDto(patched, expand);
    }

    private Patcher findPatcher(String modelName) {
        PatcherService<? extends Model> service = EverythingServices.findServiceForModel(modelName, patcherServices);
        if (service != null) {
            return new NonDefaultPatcher<>(service);
        }

        return defaultPatcher;
    }

    @Override
    public void remove(Descriptor id) {
        checkDataSecurityAllowsRemoval(id);

        String modelName = modelNames.determineModelName(id);
        Remover remover = findRemover(modelName);
        remover.remove(id.getInternalId());
        LOGGER.debug(format("Model with ID '%s' was removed by service '%s'", id, remover));
    }

    private void checkDataSecurityAllowsRemoval(Descriptor id) {
        Model model = modelRetriever.retrieveModel(id);
        dataSecurity.checkRemovalAllowed(model);
    }

    private Remover findRemover(String modelName) {
        RemovalService removalService = EverythingServices.findServiceForModel(modelName, removalServices);
        if (removalService != null) {
            return new NonDefaultRemover(removalService);
        }

        return defaultRemover;
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
            Model host = modelRetriever.retrieveModel(owned.getHostId());
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
