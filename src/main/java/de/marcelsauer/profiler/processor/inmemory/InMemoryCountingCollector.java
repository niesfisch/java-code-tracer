package de.marcelsauer.profiler.processor.inmemory;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author msauer
 */
public class InMemoryCountingCollector {

    private static final Logger LOGGER = Logger.getLogger(InMemoryCountingCollector.class);

    private static final Map<String, Integer> stacks = new HashMap<>();

    public static synchronized void increment(String trace) {
        stacks.merge(trace, 1, Integer::sum);
        LOGGER.debug("collected " + stacks.size() + " unique stacks so far.");
    }

    public static Map<String, Integer> getCollectedStacks() {
        return Collections.unmodifiableMap(stacks);
    }

    public static void purgeCollectedStacks() {
        stacks.clear();
    }
}
