package com.extremum.subscription.processor;

import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.subscription.annotation.CapturedModel;
import com.extremum.subscription.listener.WatchListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Proxy invocation handler for {@link com.extremum.everything.services.management.PatchFlow}.
 */
@Slf4j
final class PatchFlowInvocationHandler extends WatchInvocationHandler {
    private final ModelClasses modelClasses;
    private final ObjectMapper objectMapper;

    PatchFlowInvocationHandler(List<WatchListener> watchListeners, Object originalBean, ModelClasses modelClasses, ObjectMapper objectMapper) {
        super(watchListeners, originalBean);
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

            getWatchListeners().forEach(watchListener -> {
                try {
                    watchListener.onWatch(patchString);
                } catch (Exception e) {
                    log.error("Exception in onWatch() with patch {} : {}", patchString, e);
                }
            });
        }
        return method.invoke(super.getOriginalBean(), args);
    }

    private boolean isModelWatched(Object descriptor) {
        return modelClasses.getClassByModelName(((Descriptor) descriptor).getModelType())
                .getAnnotation(CapturedModel.class) != null;
    }

    private Object getPatch(Object[] args) {
        return args[1];
    }

    private boolean isPatchMethod(Method method) {
        return method.getName().equals("patch")
                && Arrays.equals(method.getParameterTypes(), new Class[]{Descriptor.class, JsonPatch.class});
    }
}
