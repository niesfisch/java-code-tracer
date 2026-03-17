package de.marcelsauer.profiler.transformer;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.instrumenter.DefaultInstrumentationCallback;
import de.marcelsauer.profiler.instrumenter.Instrumenter;
import de.marcelsauer.profiler.transformer.filter.CombinedFilter;
import org.apache.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author msauer
 */
public class Transformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(Transformer.class);
    private static final String[] HARD_SKIPPED_PREFIXES = new String[]{
        "de/marcelsauer/profiler/",
        "java/",
        "javax/",
        "jdk/",
        "sun/",
        "com/sun/",
        "org/slf4j/",
        "ch/qos/logback/",
        "org/apache/log4j/",
        "org/apache/logging/log4j/",
        "javassist/",
        "net/bytebuddy/",
        "org/objectweb/asm/",
        "cglib/",
        "org/springframework/cglib/"
    };
    private static final int DEFAULT_TRANSFORM_FAILURE_THRESHOLD = 50;
    private static final long DEFAULT_TRANSFORM_FAILURE_WINDOW_MILLIS = 30_000L;
    private static final long DEFAULT_TRANSFORM_CIRCUIT_OPEN_MILLIS = 60_000L;

    private final CombinedFilter combinedFilter;
    private final Set<String> permanentlySkippedClasses = ConcurrentHashMap.newKeySet();
    private final Object transformCircuitLock = new Object();
    private final int transformFailureThreshold;
    private final long transformFailureWindowMillis;
    private final long transformCircuitOpenMillis;

    private long transformFailureWindowStartMillis;
    private int transformFailuresInWindow;
    private long transformCircuitOpenUntilMillis;

    Instrumenter instrumenter;

    public Transformer(Config config) {
        this(config,
            DEFAULT_TRANSFORM_FAILURE_THRESHOLD,
            DEFAULT_TRANSFORM_FAILURE_WINDOW_MILLIS,
            DEFAULT_TRANSFORM_CIRCUIT_OPEN_MILLIS);
    }

    Transformer(Config config, int transformFailureThreshold, long transformFailureWindowMillis, long transformCircuitOpenMillis) {
        this.instrumenter = new Instrumenter(new DefaultInstrumentationCallback());
        this.combinedFilter = new CombinedFilter(config);
        this.transformFailureThreshold = Math.max(1, transformFailureThreshold);
        this.transformFailureWindowMillis = Math.max(1_000L, transformFailureWindowMillis);
        this.transformCircuitOpenMillis = Math.max(1_000L, transformCircuitOpenMillis);
        logger.debug("using transformer: " + Transformer.class.getName());
    }

    public byte[] transform(ClassLoader loader, String className, Class klass, ProtectionDomain domain, byte[] byteCode) throws IllegalClassFormatException {
        try {
            if (loader == null || className == null) {
                logger.debug(String.format("[skipping bootstrap class] '%s'.", className));
                return byteCode;
            }

            if (isHardSkipped(className) || permanentlySkippedClasses.contains(className)) {
                return byteCode;
            }

            if (isTransformCircuitOpen()) {
                return byteCode;
            }

            boolean shouldBeInstrumented = combinedFilter.matches(className);
            if (!shouldBeInstrumented) {
                return byteCode;
            }

            logger.debug(String.format("[instrumenting] '%s'.", className));
            byte[] transformed = this.instrumenter.instrument(className, loader);
            // null means "skip transformation" per ClassFileTransformer contract
            return transformed != null ? transformed : byteCode;
        } catch (Throwable t) {
            if (className != null) {
                permanentlySkippedClasses.add(className);
            }
            registerTransformFailure(className, t);
            // Never let any exception escape the transformer — the JVM JPLIS layer
            // turns uncaught throwables into the ugly "!errorOutstanding" native assertion.
            logger.warn(String.format("transform failed for '%s', returning original bytecode: %s", className, t.getMessage()));
            return byteCode;
        }
    }

    private boolean isHardSkipped(String className) {
        for (String hardSkippedPrefix : HARD_SKIPPED_PREFIXES) {
            if (className.startsWith(hardSkippedPrefix)) {
                return true;
            }
        }
        return false;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private boolean isTransformCircuitOpen() {
        synchronized (transformCircuitLock) {
            return currentTimeMillis() < transformCircuitOpenUntilMillis;
        }
    }

    private void registerTransformFailure(String className, Throwable throwable) {
        synchronized (transformCircuitLock) {
            long now = currentTimeMillis();

            if (transformFailureWindowStartMillis == 0L
                || (now - transformFailureWindowStartMillis) > transformFailureWindowMillis) {
                transformFailureWindowStartMillis = now;
                transformFailuresInWindow = 0;
            }

            transformFailuresInWindow++;

            if (transformFailuresInWindow >= transformFailureThreshold) {
                transformCircuitOpenUntilMillis = now + transformCircuitOpenMillis;
                logger.warn(String.format(
                    "transform circuit opened for %dms after %d failures in %dms window. latest class='%s', cause='%s'",
                    transformCircuitOpenMillis,
                    transformFailuresInWindow,
                    transformFailureWindowMillis,
                    className,
                    throwable == null ? "n/a" : throwable.getClass().getSimpleName()));
                transformFailureWindowStartMillis = now;
                transformFailuresInWindow = 0;
            }
        }
    }

}
