package com.extremum.watch.processor;

import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.WatchSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class WatchEventDispatcherTest {
    @InjectMocks
    private WatchEventDispatcher dispatcher;

    @Mock
    private TextWatchEventRepository eventRepository;
    @Mock
    private WatchSubscriptionService watchSubscriptionService;
    @Mock
    private WatchEventNotificationSender notificationSender;

    @Test
    void whenAnEventIsDispatched_thenItShouldBeSavedWithItsSubscribersAndANotificationSendShouldBeTriggered() {
        String modelId = "the-id";
        TextWatchEvent event = new TextWatchEvent("the-patch", modelId, new WatchedModel());
        when(watchSubscriptionService.findAllSubscribersBySubscription("the-id"))
                .thenReturn(singleton("Alex"));

        dispatcher.consume(event);

        assertThat(event.getSubscribers(), is(equalTo(singleton("Alex"))));
        verify(eventRepository).save(event);
        verify(notificationSender).send(any());
    }
}