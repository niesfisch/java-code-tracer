package de.marcelsauer.profiler.transformer.filter;

import de.marcelsauer.profiler.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author msauer
 */
public class FilterChain implements Filter {

    private final Config config;
    private final List<Filter> filters = new ArrayList<Filter>();

    public FilterChain(Config config) {
        this.config = config;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public FilterResult skipInstrumentation(String fqClassName) {
        boolean doNotSkip = false;
        FilterResult result = new FilterResult(SkipReason.UNFILTERED, doNotSkip);
        for (Filter filter : filters) {
            FilterResult filterResult = filter.skipInstrumentation(fqClassName);
            if (filterResult.skip) {
                result = filterResult;
            }
        }
        return result;
    }
}
