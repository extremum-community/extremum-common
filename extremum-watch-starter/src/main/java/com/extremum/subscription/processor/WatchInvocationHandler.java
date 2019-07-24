package com.extremum.subscription.processor;

import com.extremum.subscription.listener.WatchListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
abstract class WatchInvocationHandler implements InvocationHandler {
    private final List<WatchListener> watchListeners;
    private final Object originalBean;
}
