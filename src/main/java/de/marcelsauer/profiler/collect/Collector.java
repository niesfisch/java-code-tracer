package de.marcelsauer.profiler.collect;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author msauer
 */
public class Collector {

    private static final Logger LOGGER = Logger.getLogger(Collector.class);

    private static final Map<String, Integer> stacks = new HashMap<String, Integer>();

    public static void collect(String trace) {
        if (stacks.containsKey(trace)) {
            stacks.put(trace, stacks.get(trace) + 1);
        } else {
            stacks.put(trace, 1);
        }
        LOGGER.debug("collected " + stacks.size() + " unique stacks so far.");
    }

    public static Map<String, Integer> getCollectedStacks() {
        return Collections.unmodifiableMap(stacks);
    }

    public static void purgeCollectedStacks() {
        stacks.clear();
    }
}
