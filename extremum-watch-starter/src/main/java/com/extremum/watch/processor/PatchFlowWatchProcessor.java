package com.extremum.watch.processor;

import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Processor for {@link com.extremum.everything.services.management.PatchFlow} pointcut
 */
@Service
@Slf4j
public final class PatchFlowWatchProcessor {
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

    public void process(JoinPoint jp, Model returnedModel) throws JsonProcessingException {
        Object[] args = jp.getArgs();
        if (isModelWatched(args[0])) {
            log.debug("Captured method {} with args {}", jp.getSignature().getName(), Arrays.toString(args));
            JsonPatch jsonPatch = (JsonPatch) args[1];
            String jsonPatchString = objectMapper.writeValueAsString(jsonPatch);
            log.debug("Convert JsonPatch into string {}", jsonPatchString);

            String modelInternalId = ((Descriptor) args[0]).getInternalId();
            TextWatchEvent event = new TextWatchEvent(jsonPatchString, modelInternalId, returnedModel);
            watchEventConsumer.consume(event);
        }
    }

    private boolean isModelWatched(Object descriptor) {
        return modelClasses.getClassByModelName(((Descriptor) descriptor).getModelType())
                .getAnnotation(CapturedModel.class) != null;
    }
}
