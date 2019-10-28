package io.extremum.common.pool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static java.lang.Thread.currentThread;

/**
 * Executes
 */
@Slf4j
class RunOnFlagOrPeriodically {
    private static final Object FLAG = new Object();

    private final Runnable action;

    private final BlockingQueue<Object> flagQueue = new ArrayBlockingQueue<>(1);
    private final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    public RunOnFlagOrPeriodically(Runnable action) {
        this.action = action;

        taskExecutor.execute(new ActionTask());
    }

    void raiseFlag() {
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
                    waitForFlagOr1Second();
                    action.run();
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("An exception caught while processing flag", e);
                }
            }
        }

        private void waitForFlagOr1Second() throws InterruptedException {
            flagQueue.poll(1, TimeUnit.SECONDS);
        }
    }
}
