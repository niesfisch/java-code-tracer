package de.marcelsauer.profiler.processor;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * handles async stuff and buffer to worker queue
 */
public abstract class AbstractAsyncStackProcessor implements StackProcessor {

    private static final Logger logger = Logger.getLogger(StackProcessor.class);
    private static final String JCA_PROCESSOR_THREAD = "jca-processor-thread";
    private static final int CAPACITY = 10000;

    /**
     * counters
     */
    private static AtomicInteger stacksCapturedCounter = new AtomicInteger(0);
    private static AtomicInteger successfullyProcessedStacksCounter = new AtomicInteger(0);

    private ArrayBlockingQueue<RecordingEvent> workerQueue = new ArrayBlockingQueue<>(CAPACITY);

    /**
     * acting like cron
     */
    private final ScheduledExecutorService scheduledExecutor =
        Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = new Thread(r, JCA_PROCESSOR_THREAD);
                thread.setDaemon(true);
                return thread;
            }
        });

    public void process(RecordingEvent event) {
        stacksCapturedCounter.incrementAndGet();
        try {
            workerQueue.add(event);
        } catch (IllegalStateException e) {
            // workerQueue full, discarding
        }
    }


    protected abstract void doStart();

    protected abstract void doStop();

    protected abstract void doProcess(Collection<RecordingEvent> snapshots);

    @Override
    public void start() {
        logger.info("starting " + this.getClass().getName());
        startScheduler();
        doStart();
    }

    @Override
    public void stop() {
        logger.info("shutting down " + this.getClass().getName());
        try {
            scheduledExecutor.shutdownNow();
        } catch (Exception e) {
            // ignore
        }
        try {
            scheduledExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
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
        scheduledExecutor.scheduleAtFixedRate(new WorkQueueProcessorTask(), 0, 1, TimeUnit.SECONDS);
    }

    class WorkQueueProcessorTask implements Runnable {

        @Override
        public void run() {
            try {
                ArrayBlockingQueue<RecordingEvent> snapshot;
                synchronized (workerQueue) {
                    snapshot = workerQueue;
                    workerQueue = new ArrayBlockingQueue<>(CAPACITY);
                }
                if (!snapshot.isEmpty()) {
                    doProcess(snapshot);
                    successfullyProcessedStacksCounter.addAndGet(snapshot.size());
                    logger.info(
                        String.format("delegating %d stacks. successfully processed so far %d",
                            snapshot.size(),
                            successfullyProcessedStacksCounter.intValue()));
                }
            } catch (Exception e) {
                logger.warn("problem while async handling. " + e.getMessage());
            }

        }
    }


}
