package com.extremum.watch.processor;

import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.SubscriptionService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collection;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Slf4j
abstract class WatchProcessor {
    private final ExtremumKafkaProperties kafkaProperties;
    private final KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate;
    private final TextWatchEventRepository eventRepository;
    private final SubscriptionService subscriptionService;

    void watchUpdate(TextWatchEvent event) {
        Collection<String> bySubscription = subscriptionService.findAllSubscribersBySubscription(event.getModelId());
        eventRepository.save(event);
        kafkaTemplate.send(kafkaProperties.getTopic(), event.toDto(bySubscription));
    }

    protected abstract void process(JoinPoint jp) throws Exception;
}
