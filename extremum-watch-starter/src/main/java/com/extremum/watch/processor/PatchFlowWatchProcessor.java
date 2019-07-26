package com.extremum.watch.processor;

import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Aspect processor for {@link com.extremum.everything.services.management.PatchFlow}.
 */
@Service
@Slf4j
public final class PatchFlowWatchProcessor extends WatchProcessor {
    private final ModelClasses modelClasses;
    private final ObjectMapper objectMapper;

    public PatchFlowWatchProcessor(ModelClasses modelClasses,
                                   ObjectMapper objectMapper, TextWatchEventRepository repository,
                                   KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate, ExtremumKafkaProperties properties) {
        super(properties, repository, kafkaTemplate);
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(JoinPoint jp) throws JsonProcessingException {
        Object[] args = jp.getArgs();
        if (isModelWatched(args[0])) {
            log.debug("Captured method {} with args {}", jp.getSignature().getName(), Arrays.toString(args));
            String patchString = objectMapper.writeValueAsString(getPatch(args));
            log.debug("Convert JsonPatch into string {}", patchString);

            TextWatchEvent event = new TextWatchEvent("patch", patchString);
            watchUpdate(event);
        }
    }

    private Object getPatch(Object[] args) {
        return args[1];
    }

    private boolean isModelWatched(Object descriptor) {
        return modelClasses.getClassByModelName(((Descriptor) descriptor).getModelType())
                .getAnnotation(CapturedModel.class) != null;
    }
}
