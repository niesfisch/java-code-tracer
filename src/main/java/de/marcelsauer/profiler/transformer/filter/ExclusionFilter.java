package de.marcelsauer.profiler.transformer.filter;

import de.marcelsauer.profiler.config.Config;

/**
 * @author msauer
 */
public class ExclusionFilter implements Filter {

    private final Config config;

    public ExclusionFilter(Config config) {
        this.config = config;
    }
    
    public FilterResult skipInstrumentation(String fqClassName) {
        FilterResult result = FilterResult.UNFILTERED;
        for (String fqClassRegex : config.classes.excluded) {
            if (fqClassName.matches(fqClassRegex)) {
                result = new FilterResult(SkipReason.EXCLUDED, true);
                break;
            }
        }
        return result;
    }
}
