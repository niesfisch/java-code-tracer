package de.marcelsauer.profiler.transformer.filter;

/**
 * @author msauer
 */
public class FilterResult {

    static final FilterResult UNFILTERED = new FilterResult(SkipReason.UNFILTERED, false);

    public final SkipReason skipReason;
    public final boolean skip;

    public FilterResult(SkipReason skipReason, boolean skip) {
        this.skipReason = skipReason;
        this.skip = skip;
    }
}
