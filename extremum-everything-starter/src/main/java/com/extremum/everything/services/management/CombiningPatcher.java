package com.extremum.everything.services.management;

import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import com.extremum.everything.security.EverythingDataSecurity;
import com.extremum.everything.services.RequestDtoValidator;
import com.extremum.sharedmodels.descriptor.Descriptor;
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
public final class CombiningPatcher implements InternalPatcher {
    private final ModelRetriever modelRetriever;
    private final ModelSaver modelSaver;
    private final DtoConversionService dtoConversionService;
    private final ObjectMapper jsonMapper;
    private final EmptyFieldDestroyer emptyFieldDestroyer;
    private final RequestDtoValidator dtoValidator;
    private final EverythingDataSecurity dataSecurity;

    public CombiningPatcher(ModelRetriever modelRetriever,
            ModelSaver modelSaver,
            DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
            EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator,
            EverythingDataSecurity dataSecurity) {
        this.modelSaver = modelSaver;
        Objects.requireNonNull(modelRetriever, "modelRetriever cannot be null");
        Objects.requireNonNull(modelSaver, "modelSaver cannot be null");
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
        this.modelRetriever = modelRetriever;
    }

    public final Model patch(Descriptor id, JsonPatch patch) {
        beforePatch(id, patch);

        Model modelToPatch = findModel(id);

        dataSecurity.checkPatchAllowed(modelToPatch);

        RequestDto patchedDto = applyPatch(patch, modelToPatch);
        validateRequest(patchedDto);

        Model patchedModel = assemblePatchedModel(patchedDto, modelToPatch);

        Model savedModel = saveWithHooks(modelToPatch, patchedModel);

        log.debug("Model with id {} has been patched with patch {}", id, patch);
        afterPatch();
        return savedModel;
    }

    private Model findModel(Descriptor id) {
        return modelRetriever.retrieveModel(id);
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
        beforeValidation(dto);

        Set<ConstraintViolation<RequestDto>> constraintViolation = dtoValidator.validate(dto);
        afterValidation(dto, !constraintViolation.isEmpty(), constraintViolation);
    }

    private Model assemblePatchedModel(RequestDto patchedDto, Model modelToPatch) {
        Model patchedModel = dtoConversionService.convertFromRequestDto(modelToPatch.getClass(), patchedDto);
        modelToPatch.copyServiceFieldsTo(patchedModel);
        return patchedModel;
    }

    private Model saveWithHooks(Model originalModel, Model patchedModel) {
        PatchSaveContext<Model> context = new PatchSaveContext<>(originalModel, patchedModel);

        beforeSave(context);

        Model savedModel = save(context);

        context.setCurrentStateModel(savedModel);
        afterSave(context);

        return context.getCurrentStateModel();
    }

    private Model save(PatchSaveContext<Model> context) {
        return modelSaver.saveModel(context.currentStateModel);
    }

    //    Methods to override if needed

    protected void beforePatch(Descriptor id, JsonPatch patch) {
    }

    protected void beforeValidation(RequestDto dto) {
    }

    protected void afterValidation(RequestDto dto, boolean hasValidationErrors, Set<ConstraintViolation<RequestDto>> constraintsViolation) {
        if (hasValidationErrors) {
            log.error("Invalid requestDto DTO after patching detected {}", constraintsViolation);
            throw new RequestDtoValidationException(dto, constraintsViolation);
        }
    }

    protected void beforeSave(PatchSaveContext<Model> context) {
    }

    protected void afterSave(PatchSaveContext<Model> context) {
    }

    protected void afterPatch() {
    }

    @Getter
    @Setter
    protected static class PatchSaveContext<M extends Model> {
        /**
         * Found by ID model. Before patching
         */
        private final M originalModel;
        private final M patchedModel;

        private M currentStateModel;

        private PatchSaveContext(M originalModel, M patchedModel) {
            this.originalModel = originalModel;
            this.patchedModel = patchedModel;

            currentStateModel = originalModel;
        }

        public String modelName() {
            return ModelUtils.getModelName(originalModel);
        }
    }
}
