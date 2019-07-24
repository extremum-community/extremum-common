package com.extremum.subscription.processor;

import com.extremum.subscription.listener.WatchListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Slf4j
abstract class WatchInvocationHandler implements InvocationHandler {
    private final List<WatchListener> watchListeners;
    private final Object originalBean;

    public void watchUpdate(String patchString) {
        watchListeners.forEach(watchListener -> {
            try {
                watchListener.onEvent(patchString);
            } catch (Exception e) {
                log.error("Exception in onEvent() with patch {} : {}", patchString, e);
            }
        });
    }
}
