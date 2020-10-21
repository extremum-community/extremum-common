package io.extremum.watch.processor;

import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.repositories.ReactiveTextWatchEventRepository;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
@ConditionalOnBean(ReactiveWatchConfiguration.class)
public final class ReactiveWatchEventDispatcher implements WatchEventConsumer {
    private final ReactiveTextWatchEventRepository eventRepository;
    private final ReactiveWatchSubscriptionService watchSubscriptionService;
    private final WatchEventNotificationSender notificationSender;

    @Override
    public void consume(TextWatchEvent event) {
        Collection<String> subscribers = watchSubscriptionService.findAllSubscribersBySubscription(event.getModelId());

        event.setSubscribers(collectionToSet(subscribers));
        eventRepository
                .save(event)
                .doOnSuccess(e -> notificationSender.send(event.toDto(subscribers)))
                .subscribe();
    }

    private Set<String> collectionToSet(Collection<String> subscribers) {
        Set<String> set = new HashSet<>(subscribers);
        return Collections.unmodifiableSet(set);
    }
}
