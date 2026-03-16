package de.marcelsauer.profiler.processor;

import java.util.Collections;
import java.util.List;

public class RecordingEvent {
    public final Stack stack;
    public final long timestampMillis;
    private final int stackHash;

    public RecordingEvent(Stack stack) {
        this.stack = stack;
        this.timestampMillis = System.currentTimeMillis();
        this.stackHash = calculateStackHash();
    }

    List<Stack.StackEntry> getStackEntries() {
        return Collections.unmodifiableList(stack.stackEntries);
    }

    public String asJson() {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{');
        sb.append("\"timestampMillis\" : \"").append(timestampMillis).append("\", ");
        sb.append("\"stack\" : [");

        boolean first = true;
        for (Stack.StackEntry call : getStackEntries()) {
            if (!first) {
                sb.append(',');
            }
            sb.append('"').append(call.methodName).append('"');
            first = false;
        }
        sb.append("]}");
        return sb.toString();
    }

    public int getStackHash() {
        return stackHash;
    }

    private int calculateStackHash() {
        int result = 1;
        for (Stack.StackEntry entry : getStackEntries()) {
            result = 31 * result + (entry.methodName != null ? entry.methodName.hashCode() : 0);
        }
        return result;
    }
}
