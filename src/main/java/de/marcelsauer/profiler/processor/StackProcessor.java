package de.marcelsauer.profiler.processor;

public interface StackProcessor {

    void process(RecordingEvent event);

    void start();

    void stop();
}
