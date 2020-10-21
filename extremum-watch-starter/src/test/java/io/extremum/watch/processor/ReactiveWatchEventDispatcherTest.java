package io.extremum.watch.processor;

import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.repositories.ReactiveTextWatchEventRepository;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveWatchEventDispatcherTest {
    @InjectMocks
    private ReactiveWatchEventDispatcher dispatcher;

    @Mock
    private ReactiveTextWatchEventRepository eventRepository;
    @Mock
    private ReactiveWatchSubscriptionService watchSubscriptionService;
    @Mock
    private WatchEventNotificationSender notificationSender;

    @Test
    void whenAnEventIsDispatched_thenItShouldBeSavedWithItsSubscribersAndANotificationSendShouldBeTriggered() {
        String modelId = "the-id";
        TextWatchEvent event = new TextWatchEvent("the-patch", "the-full-patch", modelId, new WatchedModel());
        when(watchSubscriptionService.findAllSubscribersBySubscription("the-id"))
                .thenReturn(singleton("Alex"));
        when(eventRepository.save(any())).thenReturn(Mono.just(event));

        dispatcher.consume(event);

        StepVerifier.create(Mono.just(true))
                .thenAwait(Duration.ofMillis(100))
                .consumeNextWith(val -> {
                    assertThat(event.getSubscribers(), is(equalTo(singleton("Alex"))));
                    verify(eventRepository).save(event);
                    verify(notificationSender).send(any());
                })
                .verifyComplete();
    }
}