package de.marcelsauer.profiler.collect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author msauer
 */
public class Statistics {
    public static final Set<String> instrumentedMethods = new HashSet<String>();
    public static final Set<String> instrumentedClasses = new HashSet<String>();

    public static void addInstrumentedClass(String fqClazz) {
        instrumentedClasses.add(fqClazz);
    }

    public static void addInstrumentedMethod(String fqMethod) {
        instrumentedMethods.add(fqMethod);
    }

    public static Set<String> getInstrumentedMethods() {
        return Collections.unmodifiableSet(instrumentedMethods);
    }

    public static Set<String> getInstrumentedClasses() {
        return Collections.unmodifiableSet(instrumentedClasses);
    }
}
