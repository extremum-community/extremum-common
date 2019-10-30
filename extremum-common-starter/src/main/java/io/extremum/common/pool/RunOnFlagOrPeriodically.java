package io.extremum.common.pool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

import static java.lang.Thread.currentThread;

/**
 * Executes
 */
@Slf4j
class RunOnFlagOrPeriodically {
    private static final Object FLAG = new Object();

    private final long checkForAllocationEachMillis;
    private final Runnable action;

    private final BlockingQueue<Object> flagQueue = new ArrayBlockingQueue<>(1);
    private final ExecutorService taskExecutor = Executors.newSingleThreadExecutor(new CustomizableThreadFactory("allocator-"));

    private volatile boolean flagEverRaised = false;

    public RunOnFlagOrPeriodically(long checkForAllocationEachMillis, Runnable action) {
        this.checkForAllocationEachMillis = checkForAllocationEachMillis;
        this.action = action;

        taskExecutor.execute(new ActionTask());
    }

    void raiseFlag() {
        flagEverRaised = true;
        flagQueue.offer(FLAG);
    }

    void shutdown() {
        taskExecutor.shutdown();
    }

    private class ActionTask implements Runnable {
        @Override
        public void run() {
            while (!currentThread().isInterrupted()) {
                try {
                    waitForFlagOrTimeBetweenAllocations();
                    if (flagEverRaised) {
                        action.run();
                    }
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("An exception caught while processing flag", e);
                }
            }
        }

        private void waitForFlagOrTimeBetweenAllocations() throws InterruptedException {
            flagQueue.poll(checkForAllocationEachMillis, TimeUnit.MILLISECONDS);
        }
    }
}
