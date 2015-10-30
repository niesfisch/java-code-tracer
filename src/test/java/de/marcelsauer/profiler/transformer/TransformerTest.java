package de.marcelsauer.profiler.transformer;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.instrumenter.Instrumenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.instrument.IllegalClassFormatException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class})
public class TransformerTest {
    private final Config config = new Config();

    @Test
    public void thatPackagesCanBeIgnored() throws IllegalClassFormatException {
        // given
        config.classes.excluded.add("^java.*");

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = new Transformer(config).transform(null, "java/lang/String", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
    }

    @Test
    public void thatNonIncludedPackagesAreNotProcessed() throws IllegalClassFormatException {
        // given
        config.classes.included.add("not.there");

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = new Transformer(config).transform(null, "de/marcelsauer/profiler/ClassA", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
    }

    @Test
    public void thatPackageCanBeIncluded() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^de.marcelsauer.*");
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;
        byte[] out = {};
        when(instrumenter.instrument(eq("de/marcelsauer/profiler/ClassA"), any(ClassLoader.class), any(Class.class))).thenReturn(out);

        // when
        byte[] outBuffer = transformer.transform(null, "de/marcelsauer/profiler/ClassA", null, null, new byte[]{});

        // then
        assertEquals(outBuffer, out);
    }
}
