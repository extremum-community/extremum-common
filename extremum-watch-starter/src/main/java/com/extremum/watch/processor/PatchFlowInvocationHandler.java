package com.extremum.watch.processor;

import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.subscription.annotation.CapturedModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Proxy invocation handler for {@link com.extremum.everything.services.management.PatchFlow}.
 */
@Slf4j
final class PatchFlowInvocationHandler extends WatchInvocationHandler {
    private final ModelClasses modelClasses;
    private final ObjectMapper objectMapper;

    PatchFlowInvocationHandler(Object proxiedBean, ModelClasses modelClasses,
                               ObjectMapper objectMapper, TextWatchEventRepository repository,
                               KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate, ExtremumKafkaProperties properties) {
        super(proxiedBean, repository, kafkaTemplate, properties);
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
    }

    /**
     * Proxy work only on method patch.
     * In other methods return the original result.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        if (isPatchMethod(method) && isModelWatched(args[0])) {
            log.debug("Captured method {} with args {}", method.getName(), Arrays.toString(args));
            String patchString = objectMapper.writeValueAsString(getPatch(args));
            log.debug("Convert JsonPatch into string {}", patchString);

            TextWatchEvent event = new TextWatchEvent("patch", patchString);
            watchUpdate(event);
        }
        return method.invoke(super.getOriginalBean(), args);
    }

    private Object getPatch(Object[] args) {
        return args[1];
    }

    private boolean isModelWatched(Object descriptor) {
        return modelClasses.getClassByModelName(((Descriptor) descriptor).getModelType())
                .getAnnotation(CapturedModel.class) != null;
    }

    private boolean isPatchMethod(Method method) {
        return method.getName().equals("patch")
                && Arrays.equals(method.getParameterTypes(), new Class[]{Descriptor.class, JsonPatch.class});
    }
}
