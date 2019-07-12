package com.extremum.everything.services;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ConverterNotFoundException;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import com.extremum.sharedmodels.dto.RequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
public abstract class AbstractPatcherService<M extends Model> implements PatcherService<M> {
    private final DtoConversionService dtoConversionService;
    private final ObjectMapper jsonMapper;
    private final EmptyFieldDestroyer emptyFieldDestroyer;
    private final RequestDtoValidator dtoValidator;

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
        this(dtoConversionService, jsonMapper, new PublicEmptyFieldDestroyer());
    }

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                     EmptyFieldDestroyer emptyFieldDestroyer) {
        this(dtoConversionService, jsonMapper, emptyFieldDestroyer, new DefaultRequestDtoValidator());
    }

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                     EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator) {
        Objects.requireNonNull(dtoConversionService, "dtoConversionService cannot be null");
        Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null");
        Objects.requireNonNull(emptyFieldDestroyer, "emptyFieldDestroyer cannot be null");
        Objects.requireNonNull(dtoValidator, "dtoValidator cannot be null");

        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
        this.dtoValidator = dtoValidator;
    }

    @Override
    public final M patch(String id, JsonPatch patch) {
        beforePatch(id, patch);

        M modelToPatch = findById(id);

        RequestDto patchedDto = applyPatch(patch, modelToPatch);

        validateRequest(patchedDto);

        M patchedModel = persistFromRequestDto(patchedDto, modelToPatch, id);

        log.debug("Model with id {} has been patched with patch {}", id, patch);
        afterPatch();
        return patchedModel;
    }

    private RequestDto applyPatch(JsonPatch patch, M modelToPatch) {
        JsonNode nodeToPatch = modelToJsonNode(modelToPatch);

        JsonNode patchedNode = applyPatchToNode(patch, nodeToPatch);
        ToRequestDtoConverter<M, RequestDto> converter = findConverter(modelToPatch);
        return nodeToRequestDto(patchedNode, converter);
    }

    private JsonNode modelToJsonNode(M model) {
        RequestDto requestDto = findConverter(model).convertToRequest(model, ConversionConfig.defaults());
        return jsonMapper.valueToTree(requestDto);
    }

    private String modelName(M model) {
        return ModelUtils.getModelName(model);
    }

    private ToRequestDtoConverter<M, RequestDto> findConverter(M model) {
        DtoConverter dtoConverter = dtoConversionService.findConverterOrThrow(model,
                () -> new ConverterNotFoundException(
                        "Cannot find dto converter for a model with name " + modelName(model)));

        ToRequestDtoConverter<M, RequestDto> modelConverter;
        if (dtoConverter instanceof ToRequestDtoConverter) {
            modelConverter = (ToRequestDtoConverter<M, RequestDto>) dtoConverter;
        } else {
            String message = format("Converter for a model %s is not of a %s instance",
                    modelName(model), ToRequestDtoConverter.class.getSimpleName());
            log.error(message);
            throw new ConverterNotFoundException(message);
        }
        return modelConverter;
    }

    private JsonNode applyPatchToNode(JsonPatch patch, JsonNode target) {
        try {
            return patch.apply(target);
        } catch (JsonPatchException e) {
            String message = format("Unable to apply patch %s to json %s",
                    patch, target);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private RequestDto nodeToRequestDto(JsonNode patchedNode,
            ToRequestDtoConverter<M, RequestDto> modelConverter) {
        Class<? extends RequestDto> requestDtoType = modelConverter.getRequestDtoType();

        try {
            return jsonMapper.treeToValue(patchedNode, requestDtoType);
        } catch (JsonProcessingException e) {
            String message = format("Unable to create a type %s from a raw json data %s",
                    requestDtoType, patchedNode);
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    private void validateRequest(RequestDto dto) {
        beforeValidation(dto);
        if (dtoValidator != null) {
            Set<ConstraintViolation<RequestDto>> constraintViolation = dtoValidator.validate(dto);
            afterValidation(dto, !constraintViolation.isEmpty(), constraintViolation);
        } else {
            log.warn("Cannot find request dto validator to check a patched request {}", dto);
        }
    }

    private M persistFromRequestDto(RequestDto patchedDto, M originalModel, String id) {
        PatchPersistenceContext<M> context = new PatchPersistenceContext<>(id, originalModel, patchedDto);

        beforePersist(context);
        M persistedModel = persist(context);
        context.setCurrentStateModel(persistedModel);
        afterPersist(context);
        return context.getCurrentStateModel();
    }
    
    //    Methods to override if needed

    protected void beforePatch(String id, JsonPatch patch) {
    }

    protected void beforeValidation(RequestDto dto) {
    }

    protected void afterValidation(RequestDto dto, boolean hasValidationErrors, Set<ConstraintViolation<RequestDto>> constraintsViolation) {
        if (hasValidationErrors) {
            log.error("Invalid requestDto DTO after patching detected {}", constraintsViolation);
            throw new RequestDtoValidationException(dto, constraintsViolation);
        }
    }

    protected void beforePersist(PatchPersistenceContext<M> context) {
    }

    protected void afterPersist(PatchPersistenceContext<M> context) {
    }

    protected void afterPatch() {
    }

    protected abstract M persist(PatchPersistenceContext<M> context);

    protected abstract M findById(String id);

    @Getter
    @Setter
    protected static class PatchPersistenceContext<M extends Model> {
        private final String modelId;
        /**
         * Found by ID model. Before patching
         */
        private final M originalModel;
        private final RequestDto patchedDto;

        private M currentStateModel;

        public PatchPersistenceContext(String modelId, M originalModel,
                RequestDto patchedDto) {
            this.modelId = modelId;
            this.originalModel = originalModel;
            this.patchedDto = patchedDto;

            currentStateModel = originalModel;
        }

        public String modelName() {
            return ModelUtils.getModelName(originalModel);
        }
    }
}
