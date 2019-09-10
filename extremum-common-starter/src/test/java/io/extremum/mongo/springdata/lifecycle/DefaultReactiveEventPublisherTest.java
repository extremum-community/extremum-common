package io.extremum.mongo.springdata.lifecycle;

import io.extremum.common.reactive.DefaultReactiveEventPublisher;
import io.extremum.common.reactive.ReactiveApplicationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class DefaultReactiveEventPublisherTest {

    @Mock
    private ApplicationEvent event;

    @Test
    void givenTwoListenersExist_whenAnEventIsPublished_thenBothShouldBeNotifiedOnlyAfterSubscriptionIsMade() {
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();
        DefaultReactiveEventPublisher publisher = new DefaultReactiveEventPublisher(
                Arrays.asList(listener1, listener2));

        Mono<Void> finalMono = publisher.publishEvent(event);

        assertThatNeitherWasPulled(listener1, listener2);

        finalMono.block();

        assertThatBothWerePulled(listener1, listener2);
    }

    private void assertThatNeitherWasPulled(TestListener listener1, TestListener listener2) {
        assertThat(listener1.wasPulled(), is(false));
        assertThat(listener2.wasPulled(), is(false));
    }

    private void assertThatBothWerePulled(TestListener listener1, TestListener listener2) {
        assertThat(listener1.wasPulled(), is(true));
        assertThat(listener2.wasPulled(), is(true));
    }

    @Test
    void givenTwoListenersExistAndFirstThrows_whenAnEventIsPublished_thenOnlyFirstShouldBeNotifiedAndExceptionShouldBePropagatedToNextPublishers() {
        TestListener listener1 = new TestListener(() -> {
            throw new RuntimeException("Oops!");
        });
        TestListener listener2 = new TestListener();
        DefaultReactiveEventPublisher publisher = new DefaultReactiveEventPublisher(
                Arrays.asList(listener1, listener2));

        Mono<Void> finalMono = publisher.publishEvent(event);
        try {
            finalMono.block();
            fail("An exception should be thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Oops!"));
        }

        assertThat(listener1.wasPulled(), is(true));
        assertThat(listener2.wasPulled(), is(false));
    }

    private static class TestListener implements ReactiveApplicationListener<ApplicationEvent> {
        private final Callable<Void> action;

        private final AtomicBoolean monoPulled = new AtomicBoolean();

        TestListener() {
            this(() -> null);
        }

        TestListener(Callable<Void> action) {
            this.action = action;
        }

        @Override
        public Mono<Void> onApplicationEvent(ApplicationEvent event) {
            return Mono.defer(() -> {
                monoPulled.set(true);
                return Mono.fromCallable(action);
            });
        }

        private boolean wasPulled() {
            return monoPulled.get();
        }
    }
}