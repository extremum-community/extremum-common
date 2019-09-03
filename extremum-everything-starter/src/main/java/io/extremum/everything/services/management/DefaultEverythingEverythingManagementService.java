package io.extremum.everything.services.management;

import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.Model;
import io.extremum.common.modelservices.ModelServices;
import io.extremum.everything.services.RemovalService;
import io.extremum.everything.services.defaultservices.DefaultRemover;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DefaultEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEverythingEverythingManagementService.class);

    private final ModelRetriever modelRetriever;
    private final PatchFlow patchFlow;
    private final List<RemovalService> removalServices;
    private final DefaultRemover defaultRemover;
    private final DtoConversionService dtoConversionService;
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

}
