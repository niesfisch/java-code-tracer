package de.marcelsauer.profiler.transformer.filter;

import de.marcelsauer.profiler.config.Config;

public class CombinedFilter implements Filter {

    private final Config config;
    private final GenericFilter inclusionFilter;
    private final GenericFilter exclusionFilter;

    public CombinedFilter(Config config) {
        this.config = config;
        this.inclusionFilter = new GenericFilter(config.classes.included);
        this.exclusionFilter = new GenericFilter(config.classes.excluded);
    }

    public boolean matches(String fqClassName) {
        if (exclusionFilter.matches(fqClassName)) {
            return false;
        }
        return inclusionFilter.matches(fqClassName);
    }
}
