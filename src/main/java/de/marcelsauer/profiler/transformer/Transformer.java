package de.marcelsauer.profiler.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.instrumenter.DefaultInstrumentationCallback;
import de.marcelsauer.profiler.instrumenter.Instrumenter;
import de.marcelsauer.profiler.transformer.filter.CombinedFilter;

/**
 * @author msauer
 */
public class Transformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(Transformer.class);
    private final CombinedFilter combinedFilter;

    Instrumenter instrumenter;

    public Transformer(Config config) {
        this.instrumenter = new Instrumenter(new DefaultInstrumentationCallback(config));
        this.combinedFilter = new CombinedFilter(config);
        logger.debug("using transformer: " + Transformer.class.getName());
    }

    public byte[] transform(ClassLoader loader, String className, Class klass, ProtectionDomain domain, byte[] byteCode) throws IllegalClassFormatException {
        boolean shouldBeInstrumented = combinedFilter.matches(className);
        if (shouldBeInstrumented) {
            logger.debug(String.format("[instrumenting] '%s'.", className));
            return this.instrumenter.instrument(className, loader, klass);
        }
        return byteCode;
    }

}
