package de.marcelsauer.profiler.recorder;

import java.util.LinkedList;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.processor.RecordingEvent;
import de.marcelsauer.profiler.processor.StackProcessor;
import de.marcelsauer.profiler.processor.StackProcessorFactory;

/**
 * @author msauer
 */
public class Recorder {
    private static final Logger logger = Logger.getLogger(Recorder.class);

    private static final ThreadLocal<Stack<de.marcelsauer.profiler.processor.Stack.StackEntry>> stack = new ThreadLocal<Stack<de.marcelsauer.profiler.processor.Stack.StackEntry>>() {
        @Override
        protected Stack<de.marcelsauer.profiler.processor.Stack.StackEntry> initialValue() {
            return new Stack<>();
        }
    };

    private static final ThreadLocal<LinkedList<de.marcelsauer.profiler.processor.Stack.StackEntry>> calls = new ThreadLocal<LinkedList<de.marcelsauer.profiler.processor.Stack.StackEntry>>() {
        @Override
        protected LinkedList<de.marcelsauer.profiler.processor.Stack.StackEntry> initialValue() {
            return new LinkedList<>();
        }
    };

    private static final StackProcessor stackProcessor = StackProcessorFactory.getStackProcessor();

    public static void touch() {
        // just to load the class ...
    }

    /**
     * call to this method is inserted at the beginning of each method via instrumentation.
     * do not delete, called via agent instrumentation
     */
    public static void start(String methodName) {
        de.marcelsauer.profiler.processor.Stack.StackEntry stackEntry = new de.marcelsauer.profiler.processor.Stack.StackEntry(methodName, System.nanoTime(), stack.get().size());
        debug("starting to record " + stackEntry);
        stack.get().push(stackEntry);
        calls.get().add(stackEntry);
    }

    /**
     * call to this method is inserted at the beginning of each method via instrumentation.
     * do not delete, called via agent instrumentation
     */
    public static void stop() {
        de.marcelsauer.profiler.processor.Stack.StackEntry stackEntry = stack.get().pop();
        debug("stopping to record " + stackEntry);
        stackEntry.end();
        if (stack.get().empty()) {
            recordingFinished();
        }
    }

    private static void debug(String s) {
        logger.debug(s);
    }

    private static void info(String s) {
        logger.info(s);
    }

    private static void recordingFinished() {
        debug("recording finished for stack");

        Statistics.recordingFinished();
        stackProcessor.process(new RecordingEvent(new de.marcelsauer.profiler.processor.Stack(calls.get())));

        stack.set(new Stack<de.marcelsauer.profiler.processor.Stack.StackEntry>());
        calls.set(new LinkedList<de.marcelsauer.profiler.processor.Stack.StackEntry>());
    }

}
