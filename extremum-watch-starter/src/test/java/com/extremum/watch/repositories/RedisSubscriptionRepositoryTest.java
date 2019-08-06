package com.extremum.watch.repositories;

import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.config.WatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SpringBootTest(classes = WatchTestConfiguration.class)
@TestInstance(PER_CLASS)
class RedisSubscriptionRepositoryTest extends TestWithServices {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void givenOneSubscriptionExists_whenFindingSubscribersForThisModel_thenTheSubscriberShouldBeReturned() {
        subscriptionRepository.subscribe(Collections.singletonList("dead"), "Alex");

        Collection<String> subscribers = subscriptionRepository.getAllSubscribersIdsBySubscription("dead");
        
        assertThat(subscribers, equalTo(Collections.singletonList("Alex")));
    }

    @Test
    void givenTwoSubscriptionsExistForTwoSubscribers_whenFindingSubscribersForOneModel_thenBothSubscriberShouldBeReturned() {
        subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Alex");
        subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Ben");

        Collection<String> subscribers = subscriptionRepository.getAllSubscribersIdsBySubscription("dead");

        assertThat(subscribers, hasSize(2));
        assertThat(subscribers, hasItems("Alex", "Ben"));
    }

    @Test
    void givenTwoSubscriptionsExistForTwoSubscribers_whenAlexDeletesOneSubscription_thenForItAlexShouldNotBeFound() {
        subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Alex");
        subscriptionRepository.subscribe(Arrays.asList("dead", "beef"), "Ben");
        subscriptionRepository.unsubscribe(Collections.singletonList("dead"), "Alex");

        Collection<String> subscribers = subscriptionRepository.getAllSubscribersIdsBySubscription("dead");

        assertThat(subscribers, equalTo(Collections.singletonList("Ben")));
    }
}