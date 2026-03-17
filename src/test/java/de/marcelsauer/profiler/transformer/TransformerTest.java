package de.marcelsauer.profiler.transformer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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

    @Test
    public void thatProfilerClassesAreAlwaysHardSkipped() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^de.marcelsauer.*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = transformer.transform(loader, "de/marcelsauer/profiler/recorder/Recorder", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
        verifyZeroInteractions(instrumenter);
    }

    @Test
    public void thatJdkClassesAreAlwaysHardSkipped() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^java.*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = transformer.transform(loader, "java/lang/String", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
        verifyZeroInteractions(instrumenter);
    }

    @Test
    public void thatLoggingFrameworkClassesAreAlwaysHardSkipped() throws IllegalClassFormatException {
        // given
        config.classes.included.add(".*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = transformer.transform(loader, "org/apache/log4j/Logger", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
        verifyZeroInteractions(instrumenter);
    }

    @Test
    public void thatJavassistClassesAreAlwaysHardSkipped() throws IllegalClassFormatException {
        // given
        config.classes.included.add(".*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];

        // when
        byte[] outBuffer = transformer.transform(loader, "javassist/CtClass", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
        verifyZeroInteractions(instrumenter);
    }

    @Test
    public void thatBytecodeAndProxyFrameworkClassesAreAlwaysHardSkipped() throws IllegalClassFormatException {
        // given
        config.classes.included.add(".*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;
        byte[] inBuffer = new byte[10];

        // when
        assertEquals(inBuffer, transformer.transform(loader, "net/bytebuddy/agent/ByteBuddyAgent", null, null, inBuffer));
        assertEquals(inBuffer, transformer.transform(loader, "org/objectweb/asm/ClassReader", null, null, inBuffer));
        assertEquals(inBuffer, transformer.transform(loader, "cglib/proxy/Enhancer", null, null, inBuffer));
        assertEquals(inBuffer, transformer.transform(loader, "org/springframework/cglib/proxy/Enhancer", null, null, inBuffer));

        // then
        verifyZeroInteractions(instrumenter);
    }

    @Test
    public void thatTransformerFailsOpenWhenInstrumenterThrows() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^de.marcelsauer.sample.*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];
        when(instrumenter.instrument(eq("de/marcelsauer/sample/ClassA"), eq(loader))).thenThrow(new NoClassDefFoundError("boom"));

        // when
        byte[] outBuffer = transformer.transform(loader, "de/marcelsauer/sample/ClassA", null, null, inBuffer);

        // then
        assertEquals(outBuffer, inBuffer);
    }

    @Test
    public void thatFailedClassesAreCachedAndSkippedAfterFirstFailure() throws IllegalClassFormatException {
        // given
        config.classes.included.add("^de.marcelsauer.sample.*");
        ClassLoader loader = mock(ClassLoader.class);
        Transformer transformer = new Transformer(config);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];
        when(instrumenter.instrument(eq("de/marcelsauer/sample/ClassB"), eq(loader))).thenThrow(new RuntimeException("first failure"));

        // when
        transformer.transform(loader, "de/marcelsauer/sample/ClassB", null, null, inBuffer);
        transformer.transform(loader, "de/marcelsauer/sample/ClassB", null, null, inBuffer);

        // then
        verify(instrumenter, times(1)).instrument(eq("de/marcelsauer/sample/ClassB"), eq(loader));
    }

    @Test
    public void thatTransformCircuitOpensAfterThresholdAndSkipsFurtherInstrumentation() throws IllegalClassFormatException {
        // given
        Config breakerConfig = new Config();
        breakerConfig.classes.included.add("^de.marcelsauer.sample.*");
        ClassLoader loader = mock(ClassLoader.class);
        TestableTransformer transformer = new TestableTransformer(breakerConfig, 2, 60_000L, 10_000L);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];
        when(instrumenter.instrument(anyString(), eq(loader))).thenThrow(new RuntimeException("boom"));

        // when - first two failures open the breaker
        transformer.transform(loader, "de/marcelsauer/sample/ClassA", null, null, inBuffer);
        transformer.transform(loader, "de/marcelsauer/sample/ClassB", null, null, inBuffer);
        // this call would normally instrument, but must be skipped while breaker is open
        transformer.transform(loader, "de/marcelsauer/sample/ClassC", null, null, inBuffer);

        // then
        verify(instrumenter, times(2)).instrument(anyString(), eq(loader));
    }

    @Test
    public void thatTransformCircuitAllowsRetriesAfterCooldown() throws IllegalClassFormatException {
        // given
        Config breakerConfig = new Config();
        breakerConfig.classes.included.add("^de.marcelsauer.sample.*");
        ClassLoader loader = mock(ClassLoader.class);
        TestableTransformer transformer = new TestableTransformer(breakerConfig, 2, 60_000L, 5_000L);
        Instrumenter instrumenter = mock(Instrumenter.class);
        transformer.instrumenter = instrumenter;

        byte[] inBuffer = new byte[10];
        when(instrumenter.instrument(anyString(), eq(loader))).thenThrow(new RuntimeException("boom"));

        // when - open breaker
        transformer.transform(loader, "de/marcelsauer/sample/ClassD", null, null, inBuffer);
        transformer.transform(loader, "de/marcelsauer/sample/ClassE", null, null, inBuffer);

        // advance clock beyond cooldown and try a new class
        transformer.advanceMillis(6_000L);
        transformer.transform(loader, "de/marcelsauer/sample/ClassF", null, null, inBuffer);

        // then - third call should reach instrumenter again after cooldown elapsed
        verify(instrumenter, times(3)).instrument(anyString(), eq(loader));
    }

    private static class TestableTransformer extends Transformer {
        private long nowMillis = 1_000L;

        TestableTransformer(Config config, int failureThreshold, long failureWindowMillis, long circuitOpenMillis) {
            super(config, failureThreshold, failureWindowMillis, circuitOpenMillis);
        }

        @Override
        protected long currentTimeMillis() {
            return nowMillis;
        }

        void advanceMillis(long millis) {
            nowMillis += millis;
        }
    }
}
