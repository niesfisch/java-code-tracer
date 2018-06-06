package de.marcelsauer.profiler.processor.inmemory;

import de.marcelsauer.profiler.processor.RecordingEvent;
import de.marcelsauer.profiler.processor.StackProcessor;

public class InMemoryStackProcessor implements StackProcessor {
    public void process(RecordingEvent event) {
        InMemoryCountingCollector.increment(event.stack.toString());
    }

    @Override
    public void start() {
        // noop
    }

    @Override
    public void stop() {
        // noop
    }
}
