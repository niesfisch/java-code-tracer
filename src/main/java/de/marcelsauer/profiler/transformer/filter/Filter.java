package de.marcelsauer.profiler.transformer.filter;

/**
 * @author msauer
 */
public interface Filter {
    boolean matches(String fqClassName);
}
