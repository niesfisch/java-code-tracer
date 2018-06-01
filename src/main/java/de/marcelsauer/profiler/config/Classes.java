package de.marcelsauer.profiler.config;

import java.util.HashSet;
import java.util.Set;

/**
 * @author msauer
 */
public class Classes {
    public Set<String> included = new HashSet<>();
    public Set<String> excluded = new HashSet<>();

    @Override
    public String toString() {
        return "Classes{" +
            "included=" + included +
            ", excluded=" + excluded +
            '}';
    }
}
