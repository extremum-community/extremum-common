package com.extremum.everything.services;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ConverterNotFoundException;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.util.Set;

import static java.lang.String.format;

@Getter
@Setter
@Slf4j
public abstract class AbstractPatcherService<M extends Model> implements PatcherService<M> {
    private DtoConversionService dtoConversionService;
    private ObjectMapper jsonMapper;
    private EmptyFieldDestroyer emptyFieldDestroyer = new PublicEmptyFieldDestroyer();
    private RequestDtoValidator dtoValidator = new DefaultRequestDtoValidator();

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
    }

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                     EmptyFieldDestroyer emptyFieldDestroyer) {
        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
    }

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                     EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator) {
        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
        this.dtoValidator = dtoValidator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public M patch(String id, JsonPatch patch) {
        beforePatch(id, patch);
        M foundModel = findById(id);

        String name = ModelUtils.getModelName(foundModel.getClass());
        DtoConverter dtoConverter = dtoConversionService.determineConverterOrElseThrow(foundModel,
                () -> new ConverterNotFoundException("Cannot found dto converter for a model with name " + name));

//        Validation before execution
        ToRequestDtoConverter<M, RequestDto> modelConverter;
        if (dtoConverter instanceof ToRequestDtoConverter) {
            modelConverter = (ToRequestDtoConverter<M, RequestDto>) dtoConverter;
        } else {
            String message = format("Converter for a model %s is not of a %s instance",
                    name, ToRequestDtoConverter.class.getSimpleName());
            log.error(message);
            throw new ConverterNotFoundException(message);
        }

        RequestDto requestDto = modelConverter.convertToRequest(foundModel, null);
        JsonNode jsonDtoNode = jsonMapper.valueToTree(requestDto);

        Class<? extends RequestDto> requestDtoType = modelConverter.getRequestDtoType();
        RequestDto patchedDto = applyPatch(patch, jsonDtoNode, requestDtoType);

        validateRequest(patchedDto);
        M patchedModel = persistFromRequestDto(patchedDto, foundModel, id, name);

        log.debug("Model with id {} has been patched with patch {}", id, patch);
        afterPatch();
        return patchedModel;
    }

    private RequestDto applyPatch(JsonPatch patch, JsonNode jsonDtoNode, Class<? extends RequestDto> requestDtoType) {
        try {
            JsonNode patchedNode = patch.apply(jsonDtoNode);
            return jsonMapper.treeToValue(patchedNode, requestDtoType);
        } catch (IOException e) {
            String message = format("Unable to create a type %s from a raw json data %s",
                    requestDtoType, jsonDtoNode);
            log.error(message);
            throw new RuntimeException(message);
        } catch (JsonPatchException e) {
            String message = format("Unable to apply patch %s to json %s",
                    patch, jsonDtoNode);
            log.error(message, e);
            throw new RuntimeException(message, e);
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

    private M persistFromRequestDto(RequestDto dto, M origin, String id, String modelName) {
        PatchPersistenceContext<M> context = new PatchPersistenceContext<>();
        context.setModelId(id);
        context.setOriginModel(origin);
        context.setRequestDto(dto);

        beforePersist(context);
        handleContextBeforePersisting(context);
        context.setCurrentStateModel(persist(context, modelName));
        afterPersist(context);
        return context.getCurrentStateModel();
    }


    private M destroyEmptyFields(M model) {
        if (emptyFieldDestroyer != null) {
            return emptyFieldDestroyer.destroy(model);
        } else {
            log.warn("Empty fields cannot be destroyed because EmptyFieldDestroyer doesn't defined");
            return model;
        }
    }

//    Methods to override if needed

    protected void handleContextBeforePersisting(PatchPersistenceContext<M> context) {
        context.currentStateModel = destroyEmptyFields(context.originModel);
    }

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

    protected abstract M persist(PatchPersistenceContext<M> context, String modelName);

    protected abstract M findById(String id);

    @Getter
    @Setter
    protected static class PatchPersistenceContext<M extends Model> {
        private String modelId;

        /**
         * Found by ID model. Before patching
         */
        private M originModel;

        private M currentStateModel;

        private RequestDto requestDto;
    }
}
