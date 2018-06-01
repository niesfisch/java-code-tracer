package de.marcelsauer.profiler.transformer.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.marcelsauer.profiler.config.Config;

public class CombinedFilterTest {

    private Config config = new Config();
    private Filter filter = new CombinedFilter(config);

    @Test
    public void thatInvalidArgumentsDoNotMatch() {
        assertFalse(filter.matches(null));
        assertFalse(filter.matches(""));
    }

    @Test
    public void thatClassesCanBeIncludedViaAllMatcher() {
        // given
        config.classes.included.add(".*");

        // when
        assertTrue(filter.matches("a.b.C"));
    }

    @Test
    public void thatClassesWillBeExcluded() {
        // given
        config.classes.included.add("a.b.C");
        config.classes.excluded.add("a.b.C");

        // when
        assertFalse(filter.matches("a.b.C"));
    }

    @Test
    public void thatClassesWillBeExcludedViaAllMatcher() {
        // given
        config.classes.included.add("a.b.C");
        config.classes.excluded.add(".*");

        // when
        assertFalse(filter.matches("a.b.C"));
    }

    @Test
    public void thatClassesWillBeExcludedViaAllMatcherGlobally() {
        // given
        config.classes.included.add(".*");
        config.classes.excluded.add(".*");

        // when
        assertFalse(filter.matches("a"));
        assertFalse(filter.matches("a.b.C"));
    }


}
