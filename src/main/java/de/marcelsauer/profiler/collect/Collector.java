package de.marcelsauer.profiler.collect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author msauer
 */
public class Collector {

    private static final Logger LOGGER = Logger.getLogger(Collector.class);

    private static final Map<String, Integer> stacks = new HashMap<String, Integer>();

    public static synchronized void collect(String trace) {
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
