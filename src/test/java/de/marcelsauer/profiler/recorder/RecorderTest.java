package de.marcelsauer.profiler.recorder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.marcelsauer.profiler.processor.inmemory.InMemoryCountingCollector;

public class RecorderTest {

    @Test
    public void thatStacksAreCorrectlyRecordedAndCounted() {
        String nestedMethodA = "method_A";
        String nestedMethodA_A = "method_A_A";
        String nestedMethodA_A_A = "method_A_A_A";
        String nestedMethodB = "method_B";

        Recorder.start("method_1");

        Recorder.start(nestedMethodA);
        Recorder.start(nestedMethodA_A);
        Recorder.stop();
        Recorder.start(nestedMethodA_A);
        Recorder.stop();
        Recorder.start(nestedMethodA_A);
        Recorder.start(nestedMethodA_A_A);
        Recorder.stop();
        Recorder.stop();
        Recorder.start(nestedMethodA_A);
        Recorder.stop();
        Recorder.stop();
        Recorder.start(nestedMethodA);
        Recorder.stop();
        Recorder.start(nestedMethodA);
        Recorder.stop();
        Recorder.start(nestedMethodA);
        Recorder.stop();

        Recorder.start(nestedMethodB);
        Recorder.stop();
        Recorder.start(nestedMethodB);
        Recorder.stop();
        Recorder.start(nestedMethodB);
        Recorder.stop();
        Recorder.start(nestedMethodB);
        Recorder.stop();

        Recorder.stop();

        String next = InMemoryCountingCollector.getCollectedStacks().keySet().iterator().next();
        assertEquals("[StackEntry{methodName='method_1'}, StackEntry{methodName='method_A'}, StackEntry{methodName='method_A_A'}, StackEntry{methodName='method_A_A'}, StackEntry{methodName='method_A_A'}, StackEntry{methodName='method_A_A_A'}, StackEntry{methodName='method_A_A'}, StackEntry{methodName='method_A'}, StackEntry{methodName='method_A'}, StackEntry{methodName='method_A'}, StackEntry{methodName='method_B'}, StackEntry{methodName='method_B'}, StackEntry{methodName='method_B'}, StackEntry{methodName='method_B'}]", next);

    }

}
