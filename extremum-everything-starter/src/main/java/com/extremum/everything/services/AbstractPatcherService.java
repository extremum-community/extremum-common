package com.extremum.everything.services;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import com.extremum.everything.security.EverythingDataSecurity;
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
    private final EverythingDataSecurity dataSecurity;

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EverythingDataSecurity dataSecurity) {
        this(dtoConversionService, jsonMapper, new PublicEmptyFieldDestroyer(), dataSecurity);
    }

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer,
            EverythingDataSecurity dataSecurity) {
        this(dtoConversionService, jsonMapper, emptyFieldDestroyer, new DefaultRequestDtoValidator(), dataSecurity);
    }

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            EverythingDataSecurity dataSecurity) {
        Objects.requireNonNull(dtoConversionService, "dtoConversionService cannot be null");
        Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null");
        Objects.requireNonNull(emptyFieldDestroyer, "emptyFieldDestroyer cannot be null");
        Objects.requireNonNull(dtoValidator, "dtoValidator cannot be null");
        Objects.requireNonNull(dataSecurity, "dataSecurity cannot be null");

        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
        this.dtoValidator = dtoValidator;
        this.dataSecurity = dataSecurity;
    }

    @Override
    public final M patch(String id, JsonPatch patch) {
        beforePatch(id, patch);

        M modelToPatch = findById(id);

        dataSecurity.checkPatchAllowed(modelToPatch);

        RequestDto patchedDto = applyPatch(patch, modelToPatch);
        validateRequest(patchedDto);

        M patchedModel = assermblePatchedModel(patchedDto, modelToPatch);

        M persistedModel = persistWithHooks(modelToPatch, patchedModel);

        log.debug("Model with id {} has been patched with patch {}", id, patch);
        afterPatch();
        return persistedModel;
    }

    private RequestDto applyPatch(JsonPatch patch, M modelToPatch) {
        JsonNode nodeToPatch = modelToJsonNode(modelToPatch);
        JsonNode patchedNode = applyPatchToNode(patch, nodeToPatch);
        Class<? extends RequestDto> requestDtoType = dtoConversionService.findRequestDtoType(modelToPatch.getClass());
        return nodeToRequestDto(patchedNode, requestDtoType);
    }

    private JsonNode modelToJsonNode(M model) {
        RequestDto requestDto = dtoConversionService.convertUnknownToRequestDto(model, ConversionConfig.defaults());
        return jsonMapper.valueToTree(requestDto);
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

    private RequestDto nodeToRequestDto(JsonNode patchedNode, Class<? extends RequestDto> requestDtoType) {
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

        Set<ConstraintViolation<RequestDto>> constraintViolation = dtoValidator.validate(dto);
        afterValidation(dto, !constraintViolation.isEmpty(), constraintViolation);
    }

    private M assermblePatchedModel(RequestDto patchedDto, M modelToPatch) {
        M patchedModel = dtoConversionService.convertFromRequestDto(modelToPatch.getClass(), patchedDto);
        modelToPatch.copyServiceFieldsTo(patchedModel);
        return patchedModel;
    }

    private M persistWithHooks(M originalModel, M patchedModel) {
        PatchPersistenceContext<M> context = new PatchPersistenceContext<>(originalModel, patchedModel);

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
        /**
         * Found by ID model. Before patching
         */
        private final M originalModel;
        private final M patchedModel;

        private M currentStateModel;

        public PatchPersistenceContext(M originalModel,
                M patchedModel) {
            this.originalModel = originalModel;
            this.patchedModel = patchedModel;

            currentStateModel = originalModel;
        }

        public String modelName() {
            return ModelUtils.getModelName(originalModel);
        }
    }
}
