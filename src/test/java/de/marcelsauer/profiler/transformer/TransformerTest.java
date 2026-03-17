package de.marcelsauer.profiler.transformer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.instrument.IllegalClassFormatException;

import org.junit.Test;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.instrumenter.Instrumenter;

public class TransformerTest {

    private final Config config = new Config();

    @Test
    public void thatPackagesCanBeIgnored() throws IllegalClassFormatException {
        // given
        config.classes.excluded.add("^java.*");
        ClassLoader loader = getClass().getClassLoader();

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = new Transformer(config).transform(loader, "java/lang/String", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
    }

    @Test
    public void thatNonIncludedPackagesAreNotProcessed() throws IllegalClassFormatException {
        // given
        config.classes.included.add("not.there");
        ClassLoader loader = getClass().getClassLoader();

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = new Transformer(config).transform(loader, "de/marcelsauer/profiler/ClassA", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
    }

    @Test
    public void thatPackageCanBeIncluded() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^de.marcelsauer.*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;
        byte[] out = {};
        // use a non-profiler class so the built-in self-exclusion doesn't block it
        when(instrumenter.instrument(eq("de/marcelsauer/sample/ClassA"), eq(loader))).thenReturn(out);

        // when
        byte[] outBuffer = transformer.transform(loader, "de/marcelsauer/sample/ClassA", null, null, new byte[]{});

        // then
        assertEquals(outBuffer, out);
    }

    @Test
    public void thatBootstrapClassesAreSkipped() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^java.*");
        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = new Transformer(config).transform(null, "java/lang/String", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
    }
}
