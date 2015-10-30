package de.marcelsauer.profiler.transformer.filter;

/**
 * @author msauer
 */
public interface Filter {
    FilterResult skipInstrumentation(String fqClassName);
}
