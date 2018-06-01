package de.marcelsauer.profiler.transformer.filter;

import de.marcelsauer.profiler.config.Config;

public class CombinedFilter implements Filter {

    private final Config config;
    private final RegexFilter inclusionFilter;
    private final RegexFilter exclusionFilter;

    public CombinedFilter(Config config) {
        this.config = config;
        this.inclusionFilter = new RegexFilter(config.classes.included);
        this.exclusionFilter = new RegexFilter(config.classes.excluded);
    }

    public boolean matches(String fqClassName) {
        if (exclusionFilter.matches(fqClassName)) {
            return false;
        }
        return inclusionFilter.matches(fqClassName);
    }
}
