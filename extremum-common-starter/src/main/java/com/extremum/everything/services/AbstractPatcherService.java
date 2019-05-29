package com.extremum.everything.services;

import com.extremum.common.dto.RequestDto;
import com.extremum.common.dto.converters.DtoConverter;
import com.extremum.common.dto.converters.ToRequestDtoConverter;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.destroyer.EmptyFieldDestroyer;
import com.extremum.everything.destroyer.PublicEmptyFieldDestroyer;
import com.extremum.everything.exceptions.RequestDtoValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.util.Set;

import static java.lang.String.format;

@Getter
@Setter
public abstract class AbstractPatcherService<M extends Model> implements PatcherService<M> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPatcherService.class);

    private DtoConversionService dtoConversionService;
    private ObjectMapper jsonMapper;
    private EmptyFieldDestroyer emptyFieldDestroyer = new PublicEmptyFieldDestroyer();
    private RequestDtoValidator dtoValidator = new DefaultRequestDtoValidator();

    protected AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper) {
        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
    }

    public AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                  EmptyFieldDestroyer emptyFieldDestroyer) {
        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
    }

    public AbstractPatcherService(DtoConversionService dtoConversionService, ObjectMapper jsonMapper,
                                  EmptyFieldDestroyer emptyFieldDestroyer, RequestDtoValidator dtoValidator) {
        this.dtoConversionService = dtoConversionService;
        this.jsonMapper = jsonMapper;
        this.emptyFieldDestroyer = emptyFieldDestroyer;
        this.dtoValidator = dtoValidator;
    }

    @Override
    public M patch(String id, JsonPatch patch) {
        beforePatch(id, patch);
        M found = findById(id);

        String name = ModelUtils.getModelName(found.getClass());
        DtoConverter dtoConverter = dtoConversionService.determineConverterOrElseThrow(found,
                () -> new RuntimeException("Unable to determine a DTO converter for a model " + name));

        RequestDto requestDto = convertToRequestDto(found, dtoConverter);
        String patched = patchRequest(requestDto, patch);
        RequestDto patchedRequestDto = createRequestDtoFromString(patched, dtoConverter);
        validateRequest(patchedRequestDto);
        M patchedModel = persistFromRequestDto(patchedRequestDto, found, id, name);

        LOGGER.debug("Model with id {} has been patched with patch {}", id, patch);
        afterPatch();

        return patchedModel;
    }

    private M persistFromRequestDto(RequestDto dto, M origin, String id, String modelName) {
        PatchPersistenceContext<M> context = new PatchPersistenceContext<>();
        context.setModelId(id);
        context.setOriginModel(origin);
        context.setRequestDto(dto);

        beforePersist(context);
        handleContextBeforePersisting(context);
        context.currentStateModel = persist(context, modelName);

        afterPersist(context);

        return context.currentStateModel;
    }

    private void validateRequest(RequestDto dto) {
        beforeValidation(dto);
        if (dtoValidator != null) {
            Set<ConstraintViolation<RequestDto>> constraintViolation = dtoValidator.validate(dto);
            afterValidation(dto, !constraintViolation.isEmpty(), constraintViolation);
        } else {
            LOGGER.warn("No request DTO validator will be used for validating a patched request DTO {}", dto);
        }
    }

    private RequestDto createRequestDtoFromString(String patched, DtoConverter dtoConverter) {
        if (dtoConverter instanceof ToRequestDtoConverter) {
            Class<? extends RequestDto> requestDtoType = ((ToRequestDtoConverter) dtoConverter).getRequestDtoType();
            return jsonToType(patched, requestDtoType);
        } else {
            String message = format("Converter is not of a %s instance", ToRequestDtoConverter.class.getSimpleName());
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    private RequestDto jsonToType(String patched, Class<? extends RequestDto> requestDtoType) {
        try {
            return jsonMapper.readValue(patched, requestDtoType);
        } catch (IOException e) {
            String message = format("Unable to create a type %s from a raw json data %s",
                    requestDtoType, patched);

            LOGGER.error(message);

            throw new RuntimeException(message);
        }
    }

    private String patchRequest(RequestDto requestDto, JsonPatch patch) {
        String serialized = serializeRequestDto(requestDto);
        return applyPatch(patch, serialized);
    }

    private String applyPatch(JsonPatch patch, String serialized) {
        try {
            JsonNode node = patch.apply(loadJsonNodeFromString(serialized));
            return node.toString();
        } catch (JsonPatchException e) {
            String message = format("Unable to apply patch %s to json %s", patch, serialized);
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private JsonNode loadJsonNodeFromString(String serialized) {
        try {
            return JsonLoader.fromString(serialized);
        } catch (IOException e) {
            String message = format("Unable to load a JsonNode from a string %s", serialized);
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private String serializeRequestDto(RequestDto requestDto) {
        try {
            return jsonMapper.writeValueAsString(requestDto);
        } catch (JsonProcessingException e) {
            String message = "Unable to serialize a RequestDto object";
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private RequestDto convertToRequestDto(M model, DtoConverter dtoConverter) {
        if (dtoConverter instanceof ToRequestDtoConverter) {
            return ((ToRequestDtoConverter) dtoConverter).convertToRequest(model, null);
        } else {
            String name = ModelUtils.getModelName(model.getClass());
            String message = format("Converter for a model %s is not of a %s instance",
                    name, ToRequestDtoConverter.class.getSimpleName());

            LOGGER.error(message);

            throw new RuntimeException(message);
        }
    }

    private M destroyEmptyFields(M model) {
        if (emptyFieldDestroyer != null) {
            return emptyFieldDestroyer.destroy(model);
        } else {
            LOGGER.warn("Empty fields will bot be destroyed because EmptyFieldDestroyer doesn't defined");
            return model;
        }
    }

    protected void afterPatch() {
    }

    protected void beforePatch(String id, JsonPatch patch) {
    }

    protected void afterValidation(RequestDto dto, boolean hasValidationErrors, Set<ConstraintViolation<RequestDto>> constraintsViolation) {
        if (hasValidationErrors) {
            LOGGER.error("Invalid requestDto DTO after patching detected {}", constraintsViolation);
            throw new RequestDtoValidationException(dto, constraintsViolation);
        }
    }

    protected void beforeValidation(RequestDto dto) {
    }

    protected void afterPersist(PatchPersistenceContext<M> context) {
    }

    protected abstract M persist(PatchPersistenceContext<M> context, String modelName);

    protected void handleContextBeforePersisting(PatchPersistenceContext<M> context) {
        context.currentStateModel = destroyEmptyFields(context.originModel);
    }

    protected void beforePersist(PatchPersistenceContext<M> context) {
    }

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
