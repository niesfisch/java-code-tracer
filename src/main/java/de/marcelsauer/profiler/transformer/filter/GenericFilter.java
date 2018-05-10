package de.marcelsauer.profiler.transformer.filter;

import java.util.Set;

/**
 * @author msauer
 */
public class GenericFilter implements Filter {

    private final Set<String> config;

    public GenericFilter(Set<String> config) {
        this.config = config;
    }

    public boolean matches(String fqClassName) {
        if (fqClassName == null) {
            return false;
        }
        for (String fgClassRegex : config) {
            if (fqClassName.matches(fgClassRegex)) {
                return true;
            }
        }
        return false;
    }
}
