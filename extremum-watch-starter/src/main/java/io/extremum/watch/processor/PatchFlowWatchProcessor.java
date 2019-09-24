package io.extremum.watch.processor;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.support.ModelClasses;
import io.extremum.watch.annotation.CapturedModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.everything.services.management.PatchFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Processor for {@link PatchFlow} pointcut
 */
@Service
@Slf4j
public class PatchFlowWatchProcessor {
    private final ModelClasses modelClasses;
    private final ObjectMapper objectMapper;
    private final WatchEventConsumer watchEventConsumer;

    public PatchFlowWatchProcessor(ModelClasses modelClasses,
            ObjectMapper objectMapper,
            WatchEventConsumer watchEventConsumer) {
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
        this.watchEventConsumer = watchEventConsumer;
    }

    public void process(Invocation invocation, Model returnedModel) throws JsonProcessingException {
        Object[] args = invocation.args();
        if (!isModelWatched(args[0])) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Captured method {} with args {}", invocation.methodName(), Arrays.toString(args));
        }
        JsonPatch jsonPatch = (JsonPatch) args[1];
        String jsonPatchString = objectMapper.writeValueAsString(jsonPatch);
        log.debug("Convert JsonPatch into string {}", jsonPatchString);

        String modelInternalId = ((Descriptor) args[0]).getInternalId();
        TextWatchEvent event = new TextWatchEvent(jsonPatchString, modelInternalId, returnedModel);
        watchEventConsumer.consume(event);
    }

    private boolean isModelWatched(Object descriptor) {
        return modelClasses.getClassByModelName(((Descriptor) descriptor).getModelType())
                .getAnnotation(CapturedModel.class) != null;
    }
}
