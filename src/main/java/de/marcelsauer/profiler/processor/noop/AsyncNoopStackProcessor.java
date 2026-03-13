package de.marcelsauer.profiler.processor.noop;

import de.marcelsauer.profiler.processor.AbstractAsyncStackProcessor;
import de.marcelsauer.profiler.processor.RecordingEvent;

import java.util.Collection;

public class AsyncNoopStackProcessor extends AbstractAsyncStackProcessor {
    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void doProcess(Collection<RecordingEvent> snapshots) {
        // discard
    }
}
