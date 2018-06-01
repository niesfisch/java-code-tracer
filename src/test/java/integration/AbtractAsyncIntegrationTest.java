package integration;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import de.marcelsauer.profiler.processor.AbstractAsyncStackProcessor;
import de.marcelsauer.profiler.recorder.Statistics;
import integration.package1.A;
import integration.package1.B;

/**
 * assumes you've build the project via <pre>mvn clean package</pre> so that ./target/ contains the agent jar
 */
abstract class  AbtractAsyncIntegrationTest {

    @Test
    public void thatAsyncProcessorWorks() throws Exception {
        // given
        int expectedMethodCallCount = 6;
        int callCount = 1000;
        final int expectedStackCount = expectedMethodCallCount * callCount;

        // when
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < callCount; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    A a = new A();
                    a.a();
                    randomSleep();
                    a.a();
                    a.a();

                    randomSleep();

                    B b = new B();
                    b.b();
                    b.b();
                    randomSleep();
                    b.b();
                }

                private void randomSleep() {
                    Random r = new Random();
                    int result = r.nextInt(30 - 10) + 10;
                    try {
                        Thread.sleep(result);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        new WaitOrTimeout(new WaitOrTimeout.Callback() {
            @Override
            public boolean isTrue() {
                return expectedStackCount == AbstractAsyncStackProcessor.getSuccessfullyProcessedStacksCounter();
            }
        }, expectedStackCount).go();

        // give some more time ...
        Thread.sleep(2000);

        // then
        assertEquals(expectedStackCount, Statistics.getStacksRecordedCounter());
        assertEquals(expectedStackCount, AbstractAsyncStackProcessor.getStacksCapturedCounter());
        assertEquals(expectedStackCount, AbstractAsyncStackProcessor.getSuccessfullyProcessedStacksCounter());
        assertEquals(Statistics.getStacksRecordedCounter(), AbstractAsyncStackProcessor.getStacksCapturedCounter());

        doCustomAssertions(expectedStackCount);

    }

    protected abstract void doCustomAssertions(int expectedStackCount);

}
