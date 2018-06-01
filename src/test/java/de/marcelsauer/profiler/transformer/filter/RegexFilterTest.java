package de.marcelsauer.profiler.transformer.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RegexFilterTest {
    private Set<String> config = new HashSet<>();
    private Filter filter = new RegexFilter(config);

    @Test
    public void thatInvalidArgumentsNotBeInstrumented() {
        assertFalse(filter.matches(null));
        assertFalse(filter.matches(""));
    }

    @Test
    public void thatClassesCanBeIncluded() {
        // given
        config.add("a.b.C");
        config.add("d.*");

        // when
        assertTrue(filter.matches("a.b.C"));
        assertTrue(filter.matches("d.1.2.D"));
    }

    @Test
    public void thatClassesCanBeIncludedViaAllMatcher() {
        // given
        config.add(".*");

        // when
        assertTrue(filter.matches("a"));
        assertTrue(filter.matches("a.b"));
        assertTrue(filter.matches("a.b.C"));
    }


}
