package com.extremum.watch.processor;

import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.dto.TextWatchEventNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class KafkaWatchEventNotificationSender implements WatchEventNotificationSender {
    private final ExtremumKafkaProperties kafkaProperties;
    private final KafkaTemplate<String, TextWatchEventNotificationDto> kafkaTemplate;

    @Override
    public void send(TextWatchEventNotificationDto notificationDto) {
        kafkaTemplate.send(kafkaProperties.getTopic(), notificationDto);
    }
}
