package de.marcelsauer.profiler.recorder;

import de.marcelsauer.profiler.collect.Collector;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author msauer
 */
public class Recorder {
    private static final Logger logger = Logger.getLogger(Recorder.class);

    private static final ThreadLocal<Stack<StackEntry>> stack = new ThreadLocal<Stack<StackEntry>>() {
        @Override
        protected Stack<StackEntry> initialValue() {
            return new Stack<StackEntry>();
        }
    };
    private static final ThreadLocal<LinkedList<StackEntry>> calls = new ThreadLocal<LinkedList<StackEntry>>() {
        @Override
        protected LinkedList<StackEntry> initialValue() {
            return new LinkedList<StackEntry>();
        }
    };
    private static ThreadLocal<Boolean> switchedOn = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /**
     * do not delete, called via agent instrumentation
     */
    public static void switchOn() {
        debug("starting recorder");
        switchedOn.set(true);
    }

    /**
     * do not delete, called via agent instrumentation
     */
    public static void switchOff() {
        if (!switchedOn.get()) {
            throw new IllegalStateException("recoder is not running");
        }
        debug("stopping recorder");
        switchedOn.set(false);

        recordingFinished();
    }

    /**
     * do not delete, called via agent instrumentation
     */
    public static void start(String methodName) {
        StackEntry stackEntry = new StackEntry(methodName, System.nanoTime(), stack.get().size());
        debug("starting to record " + stackEntry);
        stack.get().push(stackEntry);
        calls.get().add(stackEntry);
    }

    /**
     * do not delete, called via agent instrumentation
     */
    public static void stop() {
        StackEntry stackEntry = stack.get().pop();
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
        StringBuilder sb = new StringBuilder();
        for (StackEntry call : Recorder.getCalls()) {
            String prefix = "";
            for (int i = 0; i < call.level; i++) {
                prefix = prefix + "\t";
            }
            sb.append(String.format("%s%s\n", prefix, call.methodName));
            //info(String.format("%s%s[%dms] [%dns]", prefix, call.methodName, (call.getDuration() / 1000000), call.getDuration()));
        }
        Collector.collect(sb.toString());
        stack.get().clear();
        calls.get().clear();
    }

    public static List<StackEntry> getCalls() {
        return calls.get();
    }

    public static class StackEntry {
        public final String methodName;
        public final long startNanos;
        public long endNanos;
        public int level;

        public StackEntry(String methodName, long startNanos, int level) {
            this.methodName = methodName;
            this.startNanos = startNanos;
            this.level = level;
        }

        public void end() {
            this.endNanos = System.nanoTime();
        }

        public long getDuration() {
            return this.endNanos - this.startNanos;
        }

        @Override
        public String toString() {
            return "StackEntry{" +
                "methodName='" + methodName + '\'' +
                '}';
        }
    }
}
