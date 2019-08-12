package com.extremum.watch.repositories;

import com.extremum.common.spring.data.OffsetBasedPageRequest;
import com.extremum.watch.config.WatchTestConfiguration;
import com.extremum.watch.config.TestWithServices;
import com.extremum.watch.controller.ModelWithFilledValues;
import com.extremum.watch.models.TextWatchEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.extremum.common.mongo.MongoConstants.DISTANT_FUTURE;
import static com.extremum.common.mongo.MongoConstants.DISTANT_PAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
@SpringBootTest(classes = WatchTestConfiguration.class)
//@DataMongoTest
class TextWatchEventRepositoryTest extends TestWithServices {
    @Autowired
    private TextWatchEventRepository repository;

    private final String subscriber = randomString();

    @NotNull
    private String randomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    void whenSearchingBySubscriberInSubscribers_thenShouldFindSomething() {
        saveAnEventVisibleFor(subscriber);

        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, DISTANT_PAST, DISTANT_FUTURE, Pageable.unpaged());
        
        assertThat(events, hasSize(1));
    }

    private void saveAnEventVisibleFor(String subscriber) {
        String modelId = randomString();
        TextWatchEvent event = new TextWatchEvent("patch", modelId, new ModelWithFilledValues());
        event.setSubscribers(Collections.singleton(subscriber));
        repository.save(event);
    }

    @Test
    void whenSearchingBySubscriberNotInSubscribers_thenShouldNotFindAnything() {
        saveAnEventVisibleFor(subscriber);

        String anotherSubscriber = randomString();
        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                anotherSubscriber, DISTANT_PAST, DISTANT_FUTURE, Pageable.unpaged());

        assertThat(events, hasSize(0));
    }

    @Test
    void whenSearchingUntilYesterday_thenShouldNotFoundAnything() {
        saveAnEventVisibleFor(subscriber);

        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, DISTANT_PAST, yesterday(), Pageable.unpaged());

        assertThat(events, hasSize(0));
    }

    @NotNull
    private ZonedDateTime yesterday() {
        return ZonedDateTime.now().minusDays(1);
    }

    @Test
    void whenSearchingFromTomorrow_thenShouldNotFoundAnything() {
        saveAnEventVisibleFor(subscriber);

        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, tomorrow(), DISTANT_FUTURE, Pageable.unpaged());

        assertThat(events, hasSize(0));
    }

    @Test
    void given2EventsExist_whenSearchingWithLimit1_thenShouldOnlyFind1() {
        saveAnEventVisibleFor(subscriber);
        saveAnEventVisibleFor(subscriber);

        List<TextWatchEvent> events = repository.findBySubscribersAndCreatedBetweenOrderByCreatedAscIdAsc(
                subscriber, DISTANT_PAST, DISTANT_FUTURE, OffsetBasedPageRequest.limit(1));

        assertThat(events, hasSize(1));
    }

    @NotNull
    private ZonedDateTime tomorrow() {
        return ZonedDateTime.now().plusDays(1);
    }
}