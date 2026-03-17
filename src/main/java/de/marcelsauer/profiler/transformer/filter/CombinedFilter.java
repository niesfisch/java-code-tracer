package de.marcelsauer.profiler.transformer.filter;

import de.marcelsauer.profiler.config.Config;

public class CombinedFilter implements Filter {

    // Never instrument the agent's own classes regardless of user config.
    // Doing so causes circular class-loading and JPLIS assertion failures.
    private static final String AGENT_PACKAGE_PREFIX = "de.marcelsauer.profiler.";

    private final RegexFilter inclusionFilter;
    private final RegexFilter exclusionFilter;

    public CombinedFilter(Config config) {
        this.inclusionFilter = new RegexFilter(config.classes.included);
        this.exclusionFilter = new RegexFilter(config.classes.excluded);
    }

    public boolean matches(String fqClassName) {
        if (fqClassName == null) {
            return false;
        }
        // normalize JVM slash-format (de/foo/Bar) to canonical dot-format (de.foo.Bar)
        String canonical = fqClassName.replace('/', '.');
        // Never instrument the agent's own classes regardless of user config.
        // Doing so causes circular class-loading and JPLIS assertion failures.
        if (canonical.startsWith(AGENT_PACKAGE_PREFIX)) {
            return false;
        }
        if (exclusionFilter.matches(canonical)) {
            return false;
        }
        return inclusionFilter.matches(canonical);
    }
}
