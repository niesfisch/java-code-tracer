package de.marcelsauer.profiler.transformer;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.instrumenter.DefaultInstrumentationCallback;
import de.marcelsauer.profiler.instrumenter.Instrumenter;
import de.marcelsauer.profiler.transformer.filter.CombinedFilter;
import org.apache.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author msauer
 */
public class Transformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(Transformer.class);
    private final CombinedFilter combinedFilter;

    Instrumenter instrumenter;

    public Transformer(Config config) {
        this.instrumenter = new Instrumenter(new DefaultInstrumentationCallback());
        this.combinedFilter = new CombinedFilter(config);
        logger.debug("using transformer: " + Transformer.class.getName());
    }

    public byte[] transform(ClassLoader loader, String className, Class klass, ProtectionDomain domain, byte[] byteCode) throws IllegalClassFormatException {
        if (loader == null) {
            logger.debug(String.format("[skipping bootstrap class] '%s'.", className));
            return byteCode;
        }

        boolean shouldBeInstrumented = combinedFilter.matches(className);
        if (shouldBeInstrumented) {
            logger.debug(String.format("[instrumenting] '%s'.", className));
            return this.instrumenter.instrument(className, loader);
        }
        return byteCode;
    }

}
