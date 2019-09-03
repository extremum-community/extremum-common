package io.extremum.test.poll;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.time.Instant.now;

/**
 * @author rpuch
 */
public class Poller {
    private final Duration maxPollDuration;
    private final Duration sleepDuration = Duration.ofMillis(100);

    public Poller(Duration maxPollDuration) {
        this.maxPollDuration = maxPollDuration;
    }

    public <T> T poll(Supplier<? extends T> sampler, Predicate<? super T> finisher) throws InterruptedException {
        return poll(new CombiningProbe<T>(sampler, finisher));
    }

    public <T> T poll(Probe<T> probe) throws InterruptedException {
        Instant endTime = now().plus(maxPollDuration);

        while (now().isBefore(endTime)) {
            T value = probe.sample();
            if (probe.isFinished(value)) {
                return value;
            }

            Thread.sleep(sleepDuration.toMillis());
        }

        throw new RuntimeException("Did not sample anything matching in " + maxPollDuration);
    }

    private static class CombiningProbe<T> implements Probe<T> {
        private final Supplier<? extends T> sampler;
        private final Predicate<? super T> finisher;

        CombiningProbe(Supplier<? extends T> sampler, Predicate<? super T> finisher) {
            this.sampler = sampler;
            this.finisher = finisher;
        }

        @Override
        public T sample() {
            return sampler.get();
        }

        @Override
        public boolean isFinished(T value) {
            return finisher.test(value);
        }
    }
}
