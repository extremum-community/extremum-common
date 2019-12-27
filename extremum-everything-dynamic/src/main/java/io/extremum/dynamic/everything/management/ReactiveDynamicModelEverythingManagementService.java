package io.extremum.dynamic.everything.management;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.common.exceptions.CommonException;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.everything.services.management.ReactiveEverythingManagementService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.constant.HttpStatus;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class ReactiveDynamicModelEverythingManagementService implements ReactiveEverythingManagementService {
    private final JsonBasedDynamicModelService dynamicModelService;
    private final DynamicModelDtoConversionService dynamicModelDtoConversionService;

    @Override
    public Mono<ResponseDto> get(Descriptor id, boolean expand) {
        return dynamicModelService.findById(id)
                .onErrorMap(DescriptorNotFoundException.class,
                        cause -> newModelNotFoundException(id, cause))
                .flatMap(this::convertDynamicModelToResponseDto);
    }

    private ModelNotFoundException newModelNotFoundException(Descriptor id, Throwable cause) {
        return new ModelNotFoundException(format("Nothing was found by '%s'", id.getExternalId()), cause);
    }

    private Mono<ResponseDto> convertDynamicModelToResponseDto(Model model) {
        return dynamicModelDtoConversionService.convertToResponseDtoReactively(model, ConversionConfig.defaults());
    }

    @Override
    public Mono<ResponseDto> patch(Descriptor id, JsonPatch patch, boolean expand) {
        return dynamicModelService.findById(id)
                .map(JsonDynamicModel::getModelData)
                .map(data -> applyPatch(patch, data))
                .map(applied -> new JsonDynamicModel(id, id.getModelType(), applied))
                .flatMap(dynamicModelService::saveModel)
                .flatMap(this::convertDynamicModelToResponseDto)
                .onErrorMap(DescriptorNotFoundException.class, cause -> {
                    String msg = format("Model with id %s not found; nothing to patch with %s", id, patch);
                    log.warn(msg, cause);
                    return new ModelNotFoundException(msg, cause);
                });
    }

    private JsonNode applyPatch(JsonPatch patch, JsonNode node) {
        try {
            return patch.apply(node);
        } catch (JsonPatchException e) {
            String msg = format("Unable to apply patch %s with node %s", patch, node);
            log.error(msg, e);
            throw new CommonException(msg, HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        }
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return dynamicModelService.remove(id)
                .onErrorResume(DescriptorNotFoundException.class, _it -> Mono.empty());
    }
}
