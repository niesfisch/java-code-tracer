package de.marcelsauer.profiler.bridge;

import de.marcelsauer.profiler.recorder.Statistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BootstrapRecorderBridgeTest {

    @Test
    public void thatBridgeCanDelegateToRecorderThroughSystemClassLoader() {
        long before = Statistics.getStacksRecordedCounter();

        BootstrapRecorderBridge.start("testMethod");
        BootstrapRecorderBridge.stop();

        assertFalse(BootstrapRecorderBridge.isInitializationFailed());
        assertEquals(before + 1, Statistics.getStacksRecordedCounter());
    }
}

