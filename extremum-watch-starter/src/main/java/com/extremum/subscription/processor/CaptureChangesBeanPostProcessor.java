package com.extremum.subscription.processor;

import com.extremum.everything.services.management.PatchFlow;
import com.extremum.everything.support.ModelClasses;
import com.extremum.subscription.listener.WatchListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Component
public class CaptureChangesBeanPostProcessor implements BeanPostProcessor {
    private final List<WatchListener> watchListeners;
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!watchListeners.isEmpty()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            Optional<WatchedClass> foundType = Arrays.stream(WatchedClass.values())
                    .filter(watchedClass -> watchedClass.match(beanClass))
                    .findFirst();

            if (foundType.isPresent()) {
                WatchedClass type = foundType.get();
                log.debug("Found bean eligible for watch with class {} and type {}", beanClass, type);

                Set<Class<?>> interfaces = new HashSet<>(Arrays.asList(beanClass.getInterfaces()));
                Class<?> currentClass = beanClass;
                while (currentClass.getSuperclass() != Object.class) {
                    Class<?>[] superclassInterfaces = currentClass.getSuperclass().getInterfaces();
                    log.debug("Add interfaces {} for proxy of class {}", Arrays.toString(superclassInterfaces), beanClass);
                    interfaces.addAll(Arrays.asList(superclassInterfaces));
                    currentClass = currentClass.getSuperclass();
                }

                InvocationHandler handler = getHandler(type, bean);
                return Proxy.newProxyInstance(beanClass.getClassLoader(), interfaces.toArray(new Class[0]), handler);
            }
        }
        return bean;
    }

    private WatchInvocationHandler getHandler(WatchedClass type, Object proxiedBean) {
        switch (type) {
            case PATCHER_SERVICE:
                return new PatchFlowInvocationHandler(watchListeners, proxiedBean, modelClasses, objectMapper);
            default:
                throw new IllegalArgumentException("Cannot find implementation of invocation handler for type " + type.name());
        }
    }

    /**
     * Еnum contains possible variants of classes for watching.
     * If you try to add variant for this enum - you need to implement match method.
     * Match method uses to find that bean class belong to this variant.
     *
     * @apiNote IMPORTANT! Don't forget to add case for method getHandler()
     */
    @RequiredArgsConstructor
    enum WatchedClass {
        PATCHER_SERVICE {
            @Override
            boolean match(Class<?> clazz) {
                return PatchFlow.class.isAssignableFrom(clazz);
            }
        };

        abstract boolean match(Class<?> clazz);
    }
}
