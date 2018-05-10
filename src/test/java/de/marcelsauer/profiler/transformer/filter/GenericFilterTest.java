package de.marcelsauer.profiler.transformer.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class GenericFilterTest {
    private Set<String> config = new HashSet<String>();
    private Filter genericFilter = new GenericFilter(config);

    @Test
    public void thatInvalidArgumentsNotBeInstrumented() {
        assertFalse(genericFilter.matches(null));
        assertFalse(genericFilter.matches(""));
    }

    @Test
    public void thatClassesCanBeIncluded() {
        // given
        config.add("a.b.C");
        config.add("d.*");

        // when
        assertTrue(genericFilter.matches("a.b.C"));
        assertTrue(genericFilter.matches("d.1.2.D"));
    }

    @Test
    public void thatClassesCanBeIncludedViaAllMatcher() {
        // given
        config.add(".*");

        // when
        assertTrue(genericFilter.matches("a"));
        assertTrue(genericFilter.matches("a.b"));
        assertTrue(genericFilter.matches("a.b.C"));
    }


}
