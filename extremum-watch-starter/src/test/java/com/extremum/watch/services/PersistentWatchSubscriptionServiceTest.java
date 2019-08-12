package com.extremum.watch.services;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.repositories.SubscriptionRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class PersistentWatchSubscriptionServiceTest {
    @InjectMocks
    private PersistentWatchSubscriptionService service;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Test
    void whenSubscribing_thenAllSubscriptionsShouldBeAddedToTheRepository() {
        service.subscribe(Arrays.asList(fromInternalId("dead"), fromInternalId("beef")), "Alex");

        verify(subscriptionRepository).subscribe(Arrays.asList("dead", "beef"), "Alex");
    }
    
    private Descriptor fromInternalId(String internalId) {
        return Descriptor.builder()
                .internalId(internalId)
                .build();
    }

    @Test
    void whenUnsubscribing_thenAllSubscriptionsShouldBeRemovedFromTheRepository() {
        service.unsubscribe(Arrays.asList(fromInternalId("dead"), fromInternalId("beef")), "Alex");

        verify(subscriptionRepository).unsubscribe(Arrays.asList("dead", "beef"), "Alex");
    }

    @Test
    void whenFindingSubscriptionsBySubscriber_thenRepositoryShouldBeConsulted() {
        when(subscriptionRepository.getAllSubscribersIdsBySubscription("beef"))
                .thenReturn(Arrays.asList("Alex", "Ben"));

        Collection<String> subscribers = service.findAllSubscribersBySubscription("beef");
        assertThat(subscribers, Matchers.containsInAnyOrder("Alex", "Ben"));
    }
}