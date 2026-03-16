package de.marcelsauer.profiler.processor;

import de.marcelsauer.profiler.config.Config;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private static final long DEFAULT_STACK_HASH_RESET_INTERVAL_MILLIS = 300_000L;
    private static final long DEFAULT_STACK_HASH_RESET_INTERVAL_IN_DEDUP_MODE_MILLIS = 30_000L;

    /**
     * counters
     */
    private static final AtomicInteger stacksCapturedCounter = new AtomicInteger(0);
    private static final AtomicInteger successfullyProcessedStacksCounter = new AtomicInteger(0);

    private final BlockingQueue<RecordingEvent> workerQueue = new ArrayBlockingQueue<>(CAPACITY);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final List<RecordingEvent> drainBuffer = new ArrayList<>(DRAIN_BATCH_SIZE);
    private final List<RecordingEvent> dedupBuffer = new ArrayList<>(DRAIN_BATCH_SIZE);
    private final Set<Integer> seenStackHashes = new HashSet<>();
    private long nextStackHashResetAtMillis = 0L;
    private boolean warnedMissingDedupResetInterval;

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
        resetDedupWindow();
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

            Collection<RecordingEvent> reportableSnapshot = filterReportableStacks(snapshot);
            if (reportableSnapshot.isEmpty()) {
                continue;
            }

            doProcess(reportableSnapshot);
            successfullyProcessedStacksCounter.addAndGet(reportableSnapshot.size());
            logger.info(
                String.format("delegated %d stacks to %s. successfully processed so far %d",
                    reportableSnapshot.size(),
                    this.getClass().getSimpleName(),
                    successfullyProcessedStacksCounter.intValue()));
        }
    }

    private Collection<RecordingEvent> drainNextBatch() {
        drainBuffer.clear();
        workerQueue.drainTo(drainBuffer, DRAIN_BATCH_SIZE);
        return drainBuffer;
    }

    private Collection<RecordingEvent> filterReportableStacks(Collection<RecordingEvent> snapshot) {
        if (isReportAllStacks()) {
            return snapshot;
        }

        maybeResetSeenStackHashes();

        dedupBuffer.clear();
        for (RecordingEvent event : snapshot) {
            if (seenStackHashes.add(event.getStackHash())) {
                dedupBuffer.add(event);
            }
        }
        return dedupBuffer;
    }

    private void maybeResetSeenStackHashes() {
        long now = currentTimeMillis();
        if (now < nextStackHashResetAtMillis) {
            return;
        }

        seenStackHashes.clear();
        nextStackHashResetAtMillis = now + getStackHashResetIntervalMillis();
    }

    private void resetDedupWindow() {
        seenStackHashes.clear();
        nextStackHashResetAtMillis = currentTimeMillis() + getStackHashResetIntervalMillis();
    }

    protected boolean isReportAllStacks() {
        return Config.get().processor.reportAllStacks;
    }

    protected long getStackHashResetIntervalMillis() {
        Long configuredInterval = getConfiguredStackHashResetIntervalMillis();
        if (configuredInterval != null) {
            return Math.max(1_000L, configuredInterval);
        }

        if (!isReportAllStacks()) {
            if (!warnedMissingDedupResetInterval) {
                logger.warn("processor.stackHashResetIntervalMillis is not set while processor.reportAllStacks=false. using default 30000ms");
                warnedMissingDedupResetInterval = true;
            }
            return DEFAULT_STACK_HASH_RESET_INTERVAL_IN_DEDUP_MODE_MILLIS;
        }

        return DEFAULT_STACK_HASH_RESET_INTERVAL_MILLIS;
    }

    protected Long getConfiguredStackHashResetIntervalMillis() {
        return Config.get().processor.stackHashResetIntervalMillis;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
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
