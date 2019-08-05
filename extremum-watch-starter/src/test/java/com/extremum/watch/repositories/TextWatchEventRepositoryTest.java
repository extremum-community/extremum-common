package com.extremum.watch.repositories;

import com.extremum.common.mongo.MongoConstants;
import com.extremum.watch.config.BaseConfig;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.models.TextWatchEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
@SpringBootTest(classes = BaseConfig.class)
//@DataMongoTest
class TextWatchEventRepositoryTest extends TestWithServices {
    @Autowired
    private TextWatchEventRepository repository;

    @Test
    void whenSearchingBySubscriberInSubscribers_thenShouldFindSomething() {
        String subscriber = randomString();
        saveAnEventVisibleFor(subscriber);

        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, MongoConstants.DISTANT_PAST, MongoConstants.DISTANT_FUTURE);
        
        assertThat(events, hasSize(1));
    }

    private void saveAnEventVisibleFor(String subscriber) {
        String modelId = randomString();
        TextWatchEvent event = new TextWatchEvent("patch", modelId);
        event.setSubscribers(Collections.singleton(subscriber));
        repository.save(event);
    }

    @Test
    void whenSearchingBySubscriberNotInSubscribers_thenShouldNotFindAnything() {
        String subscriber = randomString();
        saveAnEventVisibleFor(subscriber);

        String anotherSubscriber = randomString();
        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                anotherSubscriber, MongoConstants.DISTANT_PAST, MongoConstants.DISTANT_FUTURE);

        assertThat(events, hasSize(0));
    }

    @NotNull
    private String randomString() {
        return UUID.randomUUID().toString();
    }
}