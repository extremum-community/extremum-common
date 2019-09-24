package io.extremum.everything.services.management;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.destroyer.EmptyFieldDestroyer;
import io.extremum.everything.exceptions.RequestDtoValidationException;
import io.extremum.everything.services.RequestDtoValidator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.RequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
public final class PatcherImpl implements Patcher {
    private final DtoConversionService dtoConversionService;
    private final ObjectMapper jsonMapper;
    private final EmptyFieldDestroyer emptyFieldDestroyer;
    private final RequestDtoValidator dtoValidator;
    private final PatcherHooksCollection hooksCollection;

    public PatcherImpl(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            PatcherHooksCollection hooksCollection) {
        Objects.requireNonNull(dtoConversionService, "dtoConversionService cannot be null");
        Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null");
        Objects.requireNonNull(emptyFieldDestroyer, "emptyFieldDestroyer cannot be null");
        Objects.requireNonNull(dtoValidator, "dtoValidator cannot be null");
        Objects.requireNonNull(hooksCollection, "hooksCollection cannot be null");

        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
        this.dtoValidator = dtoValidator;
        this.hooksCollection = hooksCollection;
    }

    @Override
    public final Model patch(Descriptor id, Model modelToPatch, JsonPatch patch) {
        RequestDto patchedDto = applyPatch(patch, modelToPatch);
        patchedDto = hooksCollection.afterPatchAppliedToDto(id.getModelType(), patchedDto);

        validateRequest(patchedDto);

        return assemblePatchedModel(patchedDto, modelToPatch);
    }

    private RequestDto applyPatch(JsonPatch patch, Model modelToPatch) {
        JsonNode nodeToPatch = modelToJsonNode(modelToPatch);
        JsonNode patchedNode = applyPatchToNode(patch, nodeToPatch);
        Class<? extends RequestDto> requestDtoType = dtoConversionService.findRequestDtoType(modelToPatch.getClass());
        return nodeToRequestDto(patchedNode, requestDtoType);
    }

    private JsonNode modelToJsonNode(Model model) {
        RequestDto requestDto = dtoConversionService.convertUnknownToRequestDto(model, ConversionConfig.defaults());
        return jsonMapper.valueToTree(requestDto);
    }

    private JsonNode applyPatchToNode(JsonPatch patch, JsonNode target) {
        try {
            return patch.apply(target);
        } catch (JsonPatchException e) {
            String message = format("Unable to apply patch %s to json %s", patch, target);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private RequestDto nodeToRequestDto(JsonNode patchedNode, Class<? extends RequestDto> requestDtoType) {
        try {
            return jsonMapper.treeToValue(patchedNode, requestDtoType);
        } catch (JsonProcessingException e) {
            String message = format("Unable to create a type %s from a raw json data %s", requestDtoType, patchedNode);
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    private void validateRequest(RequestDto dto) {
        Set<ConstraintViolation<RequestDto>> constraintViolation = dtoValidator.validate(dto);
        processValidationResult(dto, constraintViolation);
    }

    private Model assemblePatchedModel(RequestDto patchedDto, Model modelToPatch) {
        Model patchedModel = dtoConversionService.convertFromRequestDto(modelToPatch.getClass(), patchedDto);
        modelToPatch.copyServiceFieldsTo(patchedModel);
        return emptyFieldDestroyer.destroy(patchedModel);
    }

    private void processValidationResult(RequestDto dto,
            Set<ConstraintViolation<RequestDto>> constraintsViolation) {
        if (!constraintsViolation.isEmpty()) {
            log.error("Invalid requestDto DTO after patching detected {}", constraintsViolation);
            throw new RequestDtoValidationException(dto, constraintsViolation);
        }
    }
}
