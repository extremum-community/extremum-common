package com.extremum.watch.processor;

import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.dto.TextWatchEventNotificationDto;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.WatchSubscriptionService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base interface for all watch processors.
 * Used on the {@link com.extremum.watch.aop.CaptureChangesAspect} by different implementations.
 */
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Slf4j
abstract class WatchProcessor {
    private final ExtremumKafkaProperties kafkaProperties;
    private final KafkaTemplate<String, TextWatchEventNotificationDto> kafkaTemplate;
    private final TextWatchEventRepository eventRepository;
    private final WatchSubscriptionService watchSubscriptionService;

    void watchUpdate(TextWatchEvent event) {
        Collection<String> subscribers = watchSubscriptionService.findAllSubscribersBySubscription(event.getModelId());
        event.setSubscribers(collectionToSet(subscribers));
        eventRepository.save(event);
        kafkaTemplate.send(kafkaProperties.getTopic(), event.toDto(subscribers));
    }

    private Set<String> collectionToSet(Collection<String> subscribers) {
        Set<String> set = new HashSet<>(subscribers);
        return Collections.unmodifiableSet(set);
    }

    protected abstract void process(JoinPoint jp) throws Exception;
}
