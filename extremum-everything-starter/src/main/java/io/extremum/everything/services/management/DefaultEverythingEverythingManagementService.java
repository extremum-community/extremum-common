package io.extremum.everything.services.management;

import io.extremum.common.collection.CollectionCoordinates;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.OwnedCoordinates;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.models.BasicModel;
import io.extremum.common.models.Model;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.security.DataSecurity;
import io.extremum.everything.services.CollectionFetcher;
import io.extremum.everything.services.RemovalService;
import io.extremum.everything.services.collection.CoordinatesHandler;
import io.extremum.everything.services.collection.FetchByOwnedCoordinates;
import io.extremum.everything.services.defaultservices.DefaultRemover;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DefaultEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEverythingEverythingManagementService.class);

    private final ModelRetriever modelRetriever;
    private final PatchFlow patchFlow;
    private final List<RemovalService> removalServices;
    private final DefaultRemover defaultRemover;
    private final List<CollectionFetcher> collectionFetchers;
    private final DtoConversionService dtoConversionService;
    private final UniversalDao universalDao;
    private final DataSecurity dataSecurity;

    private final ModelNames modelNames = new ModelNames();

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        Model model = modelRetriever.retrieveModel(id);

        dataSecurity.checkGetAllowed(model);

        if (model == null) {
            throw new ModelNotFoundException(String.format("Nothing was found by '%s'", id.getExternalId()));
        }

        return convertModelToResponseDto(model, expand);
    }

    private ResponseDto convertModelToResponseDto(Model model, boolean expand) {
        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        Model patched = patchFlow.patch(id, patch);
        return convertModelToResponseDto(patched, expand);
    }

    @Override
    public void remove(Descriptor id) {
        checkDataSecurityAllowsRemoval(id);

        Remover remover = findRemover(id);
        remover.remove(id.getInternalId());
        LOGGER.debug(format("Model with ID '%s' was removed by service '%s'", id, remover));
    }

    private Remover findRemover(Descriptor id) {
        String modelName = modelNames.determineModelName(id);

        RemovalService removalService = ModelServices.findServiceForModel(modelName, removalServices);
        if (removalService != null) {
            return new NonDefaultRemover(removalService);
        }

        return defaultRemover;
    }

    private void checkDataSecurityAllowsRemoval(Descriptor id) {
        Model model = modelRetriever.retrieveModel(id);
        dataSecurity.checkRemovalAllowed(model);
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
