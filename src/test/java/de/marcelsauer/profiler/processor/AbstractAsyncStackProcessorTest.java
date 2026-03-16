package de.marcelsauer.profiler.processor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractAsyncStackProcessorTest {

    @Test
    public void thatStopFlushesPendingQueue() {
        // queue events first, then stop immediately; stop must flush remaining events.
        TestProcessor processor = new TestProcessor();
        processor.process(newEvent("m1"));
        processor.process(newEvent("m2"));

        processor.stop();

        assertEquals(2, processor.getProcessedCount());
    }

    @Test
    public void thatQueueIsDrainedInBatchesWithoutLosingEvents() {
        TestProcessor processor = new TestProcessor();
        processor.start();

        int total = 1300;
        for (int i = 0; i < total; i++) {
            processor.process(newEvent("m" + i));
        }

        processor.stop();

        assertEquals(total, processor.getProcessedCount());
        for (Integer batchSize : processor.getBatchSizes()) {
            assertTrue("batch size should stay bounded", batchSize <= 2048);
        }
    }

    @Test
    public void thatOnlyNewStacksAreReportedWhenDedupModeIsEnabled() {
        TestProcessor processor = new TestProcessor(false, 60_000L);
        processor.start();

        processor.process(newEvent("m1"));
        processor.process(newEvent("m1"));
        processor.process(newEvent("m2"));
        processor.process(newEvent("m1"));

        processor.stop();

        assertEquals(2, processor.getProcessedCount());
    }

    @Test
    public void thatSeenHashesAreResetAfterConfiguredInterval() throws InterruptedException {
        TestProcessor processor = new TestProcessor(false, 1000L);
        processor.start();

        processor.process(newEvent("m1"));
        Thread.sleep(1200L);

        processor.advanceClockMillis(2000L);
        processor.process(newEvent("m1"));
        Thread.sleep(1200L);

        processor.stop();

        assertEquals(2, processor.getProcessedCount());
    }

    private RecordingEvent newEvent(String methodName) {
        Stack.StackEntry entry = new Stack.StackEntry(methodName, 1L, 0);
        return new RecordingEvent(new Stack(Collections.singletonList(entry)));
    }

    private static class TestProcessor extends AbstractAsyncStackProcessor {
        private final boolean reportAllStacks;
        private final long stackHashResetIntervalMillis;
        private long nowMillis;
        private final AtomicInteger processedCount = new AtomicInteger();
        private final List<Integer> batchSizes = Collections.synchronizedList(new ArrayList<Integer>());

        TestProcessor() {
            this(true, 300_000L);
        }

        TestProcessor(boolean reportAllStacks, long stackHashResetIntervalMillis) {
            this.reportAllStacks = reportAllStacks;
            this.stackHashResetIntervalMillis = stackHashResetIntervalMillis;
        }

        @Override
        protected void doStart() {
            // no-op
        }

        @Override
        protected void doStop() {
            // no-op
        }

        @Override
        protected void doProcess(Collection<RecordingEvent> snapshots) {
            batchSizes.add(snapshots.size());
            processedCount.addAndGet(snapshots.size());
        }

        @Override
        protected boolean isReportAllStacks() {
            return reportAllStacks;
        }

        @Override
        protected long getStackHashResetIntervalMillis() {
            return stackHashResetIntervalMillis;
        }

        @Override
        protected long currentTimeMillis() {
            return nowMillis;
        }

        void advanceClockMillis(long millis) {
            nowMillis += millis;
        }

        int getProcessedCount() {
            return processedCount.get();
        }

        List<Integer> getBatchSizes() {
            return batchSizes;
        }
    }
}

