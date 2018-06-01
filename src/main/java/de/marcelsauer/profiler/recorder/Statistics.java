package de.marcelsauer.profiler.recorder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author msauer
 */
public class Statistics {
    private static int instrumentedMethodsCount = 0;
    private static int instrumentedClassesCount = 0;
    private static AtomicInteger stacksRecordedCounter = new AtomicInteger(0);

    public static void addInstrumentedClass(String fqClazz) {
        instrumentedClassesCount++;
    }

    public static void addInstrumentedMethod(String fqMethod) {
        instrumentedMethodsCount++;
    }

    public static int getInstrumentedMethodsCount() {
        return instrumentedMethodsCount;
    }

    public static int getInstrumentedClassesCount() {
        return instrumentedClassesCount;
    }

    public static void recordingFinished() {
        stacksRecordedCounter.incrementAndGet();
    }

    public static long getStacksRecordedCounter() {
        return stacksRecordedCounter.intValue();
    }
}
