package com.extremum.watch.processor;

import com.extremum.common.service.CommonService;
import com.extremum.everything.services.management.PatchFlow;
import com.extremum.everything.support.ModelClasses;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class CaptureChangesBeanPostProcessor implements BeanPostProcessor {
    private final TextWatchEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;
    private final ExtremumKafkaProperties properties;
    private final KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
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
        return bean;
    }

    private WatchInvocationHandler getHandler(WatchedClass type, Object proxiedBean) {
        switch (type) {
            case PATCHER_SERVICE:
                return new PatchFlowInvocationHandler(proxiedBean, modelClasses, objectMapper, eventRepository, kafkaTemplate, properties);
            case COMMON_SERVICE:
                return new CommonServiceInvocationHandler(proxiedBean, modelClasses, objectMapper, eventRepository, kafkaTemplate, properties);
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
        },
        COMMON_SERVICE {
            @Override
            boolean match(Class<?> clazz) {
                return CommonService.class.isAssignableFrom(clazz);
            }
        };

        abstract boolean match(Class<?> clazz);
    }
}
