package de.marcelsauer.profiler.config;

import java.util.HashSet;
import java.util.Set;

/**
 * @author msauer
 */
public class Classes {
    public Set<String> included = new HashSet<String>();
    public Set<String> excluded = new HashSet<String>();

    @Override
    public String toString() {
        return "Classes{" +
            "included=" + included +
            ", excluded=" + excluded +
            '}';
    }
}
