package de.marcelsauer.profiler.transformer.filter;

import de.marcelsauer.profiler.config.Config;

/**
 * @author msauer
 */
public class InclusionFilter implements Filter {

    private final Config config;

    public InclusionFilter(Config config) {
        this.config = config;
    }
    
    public FilterResult skipInstrumentation(String fqClassName) {
        FilterResult result = FilterResult.UNFILTERED;
        if (config.classes.isIncludedPackagesOnly()) {
            result = new FilterResult(SkipReason.EXCLUDED_VIA_INCLUSION, true);
            for (String fgClassRegex : config.classes.included) {
                if (fqClassName.matches(fgClassRegex)) {
                    result = FilterResult.UNFILTERED;
                    break;
                }
            }
        }
        return result;
    }
}
