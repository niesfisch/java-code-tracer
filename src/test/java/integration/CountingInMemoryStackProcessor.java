package integration;

import de.marcelsauer.profiler.processor.RecordingEvent;
import de.marcelsauer.profiler.processor.inmemory.InMemoryStackProcessor;

public class CountingInMemoryStackProcessor extends InMemoryStackProcessor {
    public static int count = 0;

    public void process(RecordingEvent event) {
        super.process(event);
        count++;
    }
}
