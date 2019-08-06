package com.extremum.watch.processor;

import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.dto.TextWatchEventNotificationDto;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.WatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public final class WatchEventDispatcher implements WatchEventConsumer {
    private final ExtremumKafkaProperties kafkaProperties;
    private final KafkaTemplate<String, TextWatchEventNotificationDto> kafkaTemplate;
    private final TextWatchEventRepository eventRepository;
    private final WatchSubscriptionService watchSubscriptionService;

    @Override
    public void consume(TextWatchEvent event) {
        Collection<String> subscribers = watchSubscriptionService.findAllSubscribersBySubscription(event.getModelId());
        event.setSubscribers(collectionToSet(subscribers));
        eventRepository.save(event);
        kafkaTemplate.send(kafkaProperties.getTopic(), event.toDto(subscribers));
    }

    private Set<String> collectionToSet(Collection<String> subscribers) {
        Set<String> set = new HashSet<>(subscribers);
        return Collections.unmodifiableSet(set);
    }
}
