package io.extremum.watch.processor;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.ModelClasses;
import io.extremum.watch.annotation.CapturedModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static io.extremum.watch.processor.JsonPatchUtils.constructFullRemovalJsonPatch;

/**
 * Processor for {@link CommonService} pointcut
 */
@RequiredArgsConstructor
@Slf4j
public abstract class CommonServiceWatchProcessorBase {
    protected final ObjectMapper objectMapper;
    protected final DescriptorService descriptorService;
    protected final ModelClasses modelClasses;
    protected final DtoConversionService dtoConversionService;
    protected final WatchEventConsumer watchEventConsumer;

    public void process(Invocation invocation, Model returnedModel) throws JsonProcessingException {
        logInvocation(invocation);

        if (isSaveMethod(invocation)) {
            processSave(invocation.args());
        } else if (isDeleteMethod(invocation)) {
            processDeletion(returnedModel, invocation.args());
        }
    }

    protected void logInvocation(Invocation invocation) {
        if (log.isDebugEnabled()) {
            log.debug("Captured method {} with args {}", invocation.methodName(), Arrays.toString(invocation.args()));
        }
    }

    protected boolean isSaveMethod(Invocation invocation) {
        return "save".equals(invocation.methodName());
    }

    protected boolean isDeleteMethod(Invocation invocation) {
        return "delete".equals(invocation.methodName());
    }

    abstract protected void processSave(Object[] args) throws JsonProcessingException;

    protected boolean isModelWatched(Model model) {
        Class<?> modelClass = model.getClass();
        return isModelClassWatched(modelClass);
    }

    protected boolean isModelClassWatched(Class<?> modelClass) {
        return modelClass.getAnnotation(CapturedModel.class) != null;
    }

    protected void processDeletion(Model returnedModel, Object[] args) throws JsonProcessingException {
        String modelInternalId = (String) args[0];
        Descriptor descriptor = descriptorService.loadByInternalId(modelInternalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Did not find a descriptor by internal ID '%s'", modelInternalId)));
        Class<Model> modelClass = modelClasses.getClassByModelName(descriptor.getModelType());

        if (isModelClassWatched(modelClass)) {
            String jsonPatch = constructFullRemovalJsonPatch(objectMapper);
            TextWatchEvent event = new TextWatchEvent(jsonPatch, null, modelInternalId, returnedModel);
            // TODO: should we just ALWAYS set modification time in CommonService.delete()?
            event.touchModelMotificationTime();
            watchEventConsumer.consume(event);
        }
    }
}
