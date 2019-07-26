package com.extremum.watch.processor;

import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.kafka.core.KafkaTemplate;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Slf4j
abstract class WatchProcessor {
    private final ExtremumKafkaProperties kafkaProperties;
    private final TextWatchEventRepository eventRepository;
    private final KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate;

    void watchUpdate(TextWatchEvent event) {
        eventRepository.save(event);
        log.debug("Send to Kafka message");
        kafkaTemplate.send(kafkaProperties.getTopic(), event.toDto());
    }

    protected abstract void process(JoinPoint jp) throws Exception;
}
