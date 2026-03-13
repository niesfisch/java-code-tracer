package de.marcelsauer.profiler.processor;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * handles async stuff and buffer to worker queue
 */
public abstract class AbstractAsyncStackProcessor implements StackProcessor {

    private static final Logger logger = Logger.getLogger(AbstractAsyncStackProcessor.class);
    private static final String JCA_PROCESSOR_THREAD = "jca-processor-thread";
    private static final int CAPACITY = 100_000;
    private static final int DRAIN_BATCH_SIZE = 2048;

    /**
     * counters
     */
    private static final AtomicInteger stacksCapturedCounter = new AtomicInteger(0);
    private static final AtomicInteger successfullyProcessedStacksCounter = new AtomicInteger(0);

    private final BlockingQueue<RecordingEvent> workerQueue = new ArrayBlockingQueue<>(CAPACITY);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final List<RecordingEvent> drainBuffer = new ArrayList<>(DRAIN_BATCH_SIZE);

    /**
     * acting like cron
     */
    private final ScheduledExecutorService scheduledExecutor =
        Executors.newScheduledThreadPool(1, r -> {
            final Thread thread = new Thread(r, JCA_PROCESSOR_THREAD);
            thread.setDaemon(true);
            return thread;
        });

    public void process(RecordingEvent event) {
        stacksCapturedCounter.incrementAndGet();
        if (!workerQueue.offer(event)) {
            logger.error("Worker queue is full");
        }
    }


    protected abstract void doStart();

    protected abstract void doStop();

    protected abstract void doProcess(Collection<RecordingEvent> snapshots);

    @Override
    public void start() {
        logger.info("starting " + this.getClass().getName());
        doStart();
        startScheduler();
    }

    @Override
    public void stop() {
        logger.info("shutting down " + this.getClass().getName());
        started.set(false);
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduledExecutor.shutdownNow();
        }

        flushQueueSafely();

        try {
            doStop();
        } catch (Exception e) {
            // ignore
        }
    }

    public static int getStacksCapturedCounter() {
        return stacksCapturedCounter.intValue();
    }

    public static int getSuccessfullyProcessedStacksCounter() {
        return successfullyProcessedStacksCounter.intValue();
    }

    private void startScheduler() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        scheduledExecutor.scheduleWithFixedDelay(new WorkQueueProcessorTask(), 0, 1, TimeUnit.SECONDS);
    }

    private void flushQueueSafely() {
        try {
            flushQueue();
        } catch (Exception e) {
            logger.warn("problem while draining queue during shutdown. " + e.getMessage());
        }
    }

    private void flushQueue() {
        while (true) {
            Collection<RecordingEvent> snapshot = drainNextBatch();
            if (snapshot.isEmpty()) {
                return;
            }
            doProcess(snapshot);
            successfullyProcessedStacksCounter.addAndGet(snapshot.size());
            logger.info(
                String.format("delegated %d stacks to %s. successfully processed so far %d",
                    snapshot.size(),
                    this.getClass().getSimpleName(),
                    successfullyProcessedStacksCounter.intValue()));
        }
    }

    private Collection<RecordingEvent> drainNextBatch() {
        drainBuffer.clear();
        workerQueue.drainTo(drainBuffer, DRAIN_BATCH_SIZE);
        return drainBuffer;
    }

    class WorkQueueProcessorTask implements Runnable {

        @Override
        public void run() {
            try {
                flushQueue();
            } catch (Exception e) {
                logger.warn("problem while async handling. " + e.getMessage());
            }

        }
    }


}
