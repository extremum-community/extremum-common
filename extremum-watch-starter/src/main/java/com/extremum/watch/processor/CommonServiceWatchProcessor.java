package com.extremum.watch.processor;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.dto.converters.ConversionConfig;
import com.extremum.common.dto.converters.services.DtoConversionService;
import com.extremum.common.exceptions.ProgrammingErrorException;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.RemoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

/**
 * Processor for {@link com.extremum.common.service.CommonService} pointcut
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class CommonServiceWatchProcessor {
    private final ObjectMapper objectMapper;
    private final DescriptorService descriptorService;
    private final ModelClasses modelClasses;
    private final DtoConversionService dtoConversionService;
    private final WatchEventConsumer watchEventConsumer;

    public void process(Invocation invocation, Model returnedModel) throws JsonProcessingException {
        logInvocation(invocation);

        if (isSaveMethod(invocation)) {
            processSave(invocation.args());
        } else if (isDeleteMethod(invocation)) {
            processDeletion(returnedModel, invocation.args());
        }
    }

    private void logInvocation(Invocation invocation) {
        if (log.isDebugEnabled()) {
            log.debug("Captured method {} with args {}", invocation.methodName(), Arrays.toString(invocation.args()));
        }
    }

    private boolean isSaveMethod(Invocation invocation) {
        return "save".equals(invocation.methodName());
    }

    private boolean isDeleteMethod(Invocation invocation) {
        return "delete".equals(invocation.methodName());
    }

    private void processSave(Object[] args) throws JsonProcessingException {
        Model model = (Model) args[0];
        if (isModelWatched(model) && model instanceof BasicModel) {
            String jsonPatchString = constructFullReplaceJsonPatch(model);
            String modelInternalId = ((BasicModel) model).getId().toString();
            TextWatchEvent event = new TextWatchEvent(jsonPatchString, modelInternalId, model);
            watchEventConsumer.consume(event);
        }
    }

    private boolean isModelWatched(Model model) {
        Class<?> modelClass = model.getClass();
        return isModelClassWatched(modelClass);
    }

    private boolean isModelClassWatched(Class<?> modelClass) {
        return modelClass.getAnnotation(CapturedModel.class) != null;
    }

    private String constructFullReplaceJsonPatch(Model model) throws JsonProcessingException {
        RequestDto dto = dtoConversionService.convertUnknownToRequestDto(model, ConversionConfig.defaults());
        ReplaceOperation operation = new ReplaceOperation(rootPointer(), new POJONode(dto));
        return serializeSingleOperationPatch(operation);
    }

    private String serializeSingleOperationPatch(JsonPatchOperation operation) throws JsonProcessingException {
        JsonPatch jsonPatch = new JsonPatch(Collections.singletonList(operation));
        return objectMapper.writeValueAsString(jsonPatch);
    }

    private JsonPointer rootPointer() {
        try {
            return new JsonPointer("/");
        } catch (JsonPointerException e) {
            throw new ProgrammingErrorException("Invalid JSON pointer", e);
        }
    }

    private void processDeletion(Model returnedModel, Object[] args) throws JsonProcessingException {
        String modelInternalId = (String) args[0];
        Descriptor descriptor = descriptorService.loadByInternalId(modelInternalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Did not find a descriptor by internal ID '%s'", modelInternalId)));
        Class<Model> modelClass = modelClasses.getClassByModelName(descriptor.getModelType());

        if (isModelClassWatched(modelClass)) {
            String jsonPatch = constructFullRemovalJsonPatch();
            TextWatchEvent event = new TextWatchEvent(jsonPatch, modelInternalId, returnedModel);
            // TODO: should we just ALWAYS set modification time in CommonService.delete()?
            event.touchModelMotificationTime();
            watchEventConsumer.consume(event);
        }
    }

    private String constructFullRemovalJsonPatch() throws JsonProcessingException {
        RemoveOperation operation = new RemoveOperation(rootPointer());
        return serializeSingleOperationPatch(operation);
    }
}
