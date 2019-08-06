package com.extremum.watch.repositories;

import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.config.WatchTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SpringBootTest(classes = WatchTestConfiguration.class)
@TestInstance(PER_CLASS)
class SubscriptionRepositoryTest extends TestWithServices {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @BeforeAll
    void setUp() {
        subscriptionRepository.subscribe(Collections.singletonList("12"), "test");
    }

    @Test
    void testPatternGet() {
        Collection<String> bySubscription = subscriptionRepository.getAllSubscribersIdsBySubscription("12");
        assertEquals(1, bySubscription.size(), "Need to find one subscriber");
    }
}