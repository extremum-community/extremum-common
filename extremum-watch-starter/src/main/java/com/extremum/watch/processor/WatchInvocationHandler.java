package com.extremum.watch.processor;

import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.InvocationHandler;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Slf4j
abstract class WatchInvocationHandler implements InvocationHandler {
    private final Object originalBean;
    private final TextWatchEventRepository eventRepository;
    private final KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate;
    private final ExtremumKafkaProperties kafkaProperties;

    void watchUpdate(TextWatchEvent event) {
        eventRepository.save(event);
        kafkaTemplate.send(kafkaProperties.getTopic(), event.toDto());
    }
}
