package com.extremum.subscription.processor;

import com.extremum.subscription.WatchListener;
import com.extremum.subscription.annotation.CaptureChanges;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Proxy invocation handler for {@link com.extremum.everything.services.PatcherService}.
 */
@Slf4j
public final class PatcherServiceInvocationHandler extends WatchInvocationHandler {
    private final ObjectMapper objectMapper;

    PatcherServiceInvocationHandler(List<WatchListener> watchListeners, Object originalBean, ObjectMapper objectMapper) {
        super(watchListeners, originalBean);
        this.objectMapper = objectMapper;
    }

    /**
     * Proxy work only on method patch.
     * In other methods return the original result.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        if (method.isAnnotationPresent(CaptureChanges.class)) {
            log.debug("Captured method {} with args {}", method.getName(), Arrays.toString(args));
            String patchString = objectMapper.writeValueAsString(args[1]);
            log.debug("Convert JsonPatch into string {}", patchString);

            getWatchListeners().forEach(watchListener -> {
                try {
                    watchListener.onWatch(patchString);
                } catch (Exception e) {
                    log.error("Exception {} on watch with patch string {}", e, patchString);
                }
            });
        }
        return method.invoke(super.getOriginalBean(), args);
    }
}
