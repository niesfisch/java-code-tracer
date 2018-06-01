package de.marcelsauer.profiler.processor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class RecordingEventTest {

    @Test
    public void thatJsonCanBeQueried() {
        // given
        Stack.StackEntry e1 = new Stack.StackEntry("m1", 123, 1);
        Stack.StackEntry e2 = new Stack.StackEntry("m2", 456, 2);
        Stack.StackEntry e3 = new Stack.StackEntry("m3", 789, 1);
        RecordingEvent event1 = new RecordingEvent(new Stack(Arrays.asList(e1, e2, e3)));

        // when
        String result = event1.asJson();

        // then
        assertEquals("{\"timestampMillis\" : \"" + event1.timestampMillis + "\", \"stack\" : [\"m1\",\"m2\",\"m3\"]}", result);
    }

    @Test
    public void thatJsonCanBeQueriedForOneCall() {
        // given
        Stack.StackEntry e1 = new Stack.StackEntry("m1", 123, 1);
        RecordingEvent event1 = new RecordingEvent(new Stack(Collections.singletonList(e1)));

        // when
        String result = event1.asJson();

        // then
        assertEquals("{\"timestampMillis\" : \"" + event1.timestampMillis + "\", \"stack\" : [\"m1\"]}", result);
    }


}
