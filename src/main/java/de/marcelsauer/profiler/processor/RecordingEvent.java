package de.marcelsauer.profiler.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RecordingEvent {
    final Stack stack;
    final long timestampMillis;

    public RecordingEvent(Stack stack) {
        this.stack = stack;
        this.timestampMillis = new Date().getTime();
    }

    List<Stack.StackEntry> getStackEntries() {
        return Collections.unmodifiableList(stack.stackEntries);
    }

    public String asJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"timestampMillis\" : \"%d\", ", timestampMillis));

        StringBuilder stackSb = new StringBuilder("[");
        for (Stack.StackEntry call : getStackEntries()) {
            stackSb.append(String.format(",\"%s\"", call.methodName));
//            stackSb.append(String.format(",\"%s%s\"", nSpaces(call), call.methodName));
        }
        stackSb.append("]");
        sb.append(String.format("\"stack\" : %s", stackSb.toString().replaceFirst(",", "")));
        sb.append("}");
        return sb.toString();
    }

    private String nSpaces(Stack.StackEntry call) {
        int n = call.level;
        char[] chars = new char[n * 2];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }
}
