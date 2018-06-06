package de.marcelsauer.profiler.processor;

import java.util.Date;
import java.util.List;

public class Stack {
    final List<StackEntry> stackEntries;

    public Stack(List<StackEntry> stackEntries) {
        this.stackEntries = stackEntries;
    }

    public static class StackEntry {
        public final String methodName;
        public final long startNanos;
        public final long timestampMillis;
        public long endNanos;
        public int level;

        public StackEntry(String methodName, long startNanos, int level) {
            this.methodName = methodName;
            this.startNanos = startNanos;
            this.level = level;
            this.timestampMillis = new Date().getTime();
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

    @Override
    public String toString() {
        return stackEntries.toString();
    }
}
