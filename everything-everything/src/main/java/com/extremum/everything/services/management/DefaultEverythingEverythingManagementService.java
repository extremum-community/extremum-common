package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.everything.exceptions.EverythingEverythingException;
import com.extremum.everything.services.EverythingEverythingService;
import com.extremum.everything.services.GetterService;
import com.extremum.everything.services.PatcherService;
import com.extremum.everything.services.RemovalService;
import com.github.fge.jsonpatch.JsonPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Service
public class DefaultEverythingEverythingManagementService implements EverythingEverythingManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEverythingEverythingManagementService.class);

    private List<GetterService<? extends Model>> getterServices;
    private List<PatcherService> patcherServices;
    private List<RemovalService> removalServices;
    private DtoConversionService dtoConversionService;

    public DefaultEverythingEverythingManagementService(List<GetterService<? extends Model>> getterServices,
                                                        List<PatcherService> patcherServices,
                                                        List<RemovalService> removalServices,
                                                        DtoConversionService dtoConversionService) {
        this.getterServices = getterServices;
        this.patcherServices = patcherServices;
        this.removalServices = removalServices;
        this.dtoConversionService = dtoConversionService;
    }

    @Override
    public ResponseDto get(Descriptor id, boolean expand) {
        String modelName = determineModelNameByIdOrThrow(id,
                () -> new EverythingEverythingException(format("Can't determine a model name for the ID %s", id)));

        GetterService getterService = findServiceForModelOrElseThrow(modelName, getterServices,
                () -> new EverythingEverythingException(
                        format("No one service of %d doesn't support getting models with name %s",
                                getterServices.size(), modelName)),
                GetterService.class);

        Model model = getterService.get(id.getInternalId());

        if (model != null) {
            LOGGER.debug(format("Model with ID %s was be found of the %s service: %s", id, getterService, model));

            ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
            return dtoConversionService.convertUnknownToResponseDto(model, conversionConfig);
        } else {
            LOGGER.debug(format("Model with ID %s wasn't be found of the %s service", id, getterService));

            return null;
        }
    }

    @Override
    public ResponseDto patch(Descriptor id, JsonPatch patch, boolean expand) {
        String modelName = determineModelNameByIdOrThrow(id,
                () -> new EverythingEverythingException(format("Can't determine a model name for the ID %s", id)));

        PatcherService patcherService = findServiceForModelOrElseThrow(modelName, patcherServices,
                () -> new EverythingEverythingException(
                        format("No one service of %d doesn't support patching models with name %s",
                                patcherServices.size(), modelName)),
                PatcherService.class);

        Model patched = patcherService.patch(id.getInternalId(), patch);

        ConversionConfig conversionConfig = ConversionConfig.builder().expand(expand).build();
        return dtoConversionService.convertUnknownToResponseDto(patched, conversionConfig);
    }

    @Override
    public boolean remove(Descriptor id) {
        String modelName = determineModelNameByIdOrThrow(id,
                () -> new EverythingEverythingException(format("Can't determine a model name for the ID %s", id)));

        RemovalService removalService = findServiceForModelOrElseThrow(modelName, removalServices,
                () -> new EverythingEverythingException(
                        format("No one service of %d doesn't support removing models with name %s",
                                removalServices.size(), modelName)),
                RemovalService.class);

        boolean removeResult = removalService.remove(id.getInternalId());
        LOGGER.debug(format("Model with ID %s %s be removed of the %s service",
                id, (removeResult ? "was" : "wasn't"), removalService));

        return removeResult;
    }

    private String determineModelNameById(Descriptor id) {
        requireNonNull(id, "ID can't be null");
        return id.getModelType();
    }

    private String determineModelNameByIdOrThrow(Descriptor id, Supplier<? extends RuntimeException> exceptionSupplier) {
        requireNonNull(id, "ID can't be null");
        requireNonNull(exceptionSupplier, "Supplier can't be null");

        String modelName = determineModelNameById(id);
        if (modelName == null) {
            LOGGER.error("Unable to determine a model name for id {}", id);
            throw exceptionSupplier.get();
        } else {
            LOGGER.debug("Model name for id {} is {}", id, modelName);
            return modelName;
        }
    }

    private <T extends EverythingEverythingService> T findServiceForModel(String modelName,
                                                                          Collection<? extends EverythingEverythingService> services,
                                                                          Class<T> expectedServiceType) {
        requireNonNull(modelName, "Name of a model can't be null");
        requireNonNull(services, "Services list can't be null");

        return services.stream()
                .parallel()
                .filter(getIsServiceSupportsModelFilter(modelName))
                .findAny()
                .filter(expectedServiceType::isInstance)
                .map(expectedServiceType::cast)
                .orElse(null);
    }

    private <T extends EverythingEverythingService> T findServiceForModelOrElseThrow(String modelName,
                                                                                     Collection<? extends EverythingEverythingService> services,
                                                                                     Supplier<? extends RuntimeException> exceptionSupplier,
                                                                                     Class<T> expectedServiceType) {
        T result = findServiceForModel(modelName, services, expectedServiceType);

        if (result == null) {
            LOGGER.error("No services for a {} model support found. {} services was be verified", modelName, services.size());
            throw exceptionSupplier.get();
        } else {
            return result;
        }
    }

    private Predicate<? super EverythingEverythingService> getIsServiceSupportsModelFilter(String modelName) {
        return service -> service.getSupportedModel().equalsIgnoreCase(modelName);
    }
}
