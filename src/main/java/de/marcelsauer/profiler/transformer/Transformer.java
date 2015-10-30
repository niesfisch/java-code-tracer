package de.marcelsauer.profiler.transformer;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.instrumenter.DefaultInstrumentationCallback;
import de.marcelsauer.profiler.instrumenter.Instrumenter;
import de.marcelsauer.profiler.transformer.filter.ExclusionFilter;
import de.marcelsauer.profiler.transformer.filter.FilterChain;
import de.marcelsauer.profiler.transformer.filter.FilterResult;
import de.marcelsauer.profiler.transformer.filter.InclusionFilter;
import de.marcelsauer.profiler.util.Util;
import org.apache.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author msauer
 */
public class Transformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(Transformer.class);
    private final Config config;
    private final FilterChain filterChain;
    Instrumenter instrumenter;

    public Transformer(Config config) {
        this.config = config;
        this.instrumenter = new Instrumenter(new DefaultInstrumentationCallback(config));
        filterChain = new FilterChain(config);
        filterChain.addFilter(new InclusionFilter(config));
        filterChain.addFilter(new ExclusionFilter(config));
        logger.debug("using transformer: " + Transformer.class.getName());
    }


    public byte[] transform(ClassLoader loader, String className, Class klass, ProtectionDomain domain, byte[] byteCode) throws IllegalClassFormatException {

        FilterResult filterResult = filterChain.skipInstrumentation(Util.fromJVM(className));

        if (filterResult.skip) {
            logger.debug(String.format("[skipping] '%s' for instrumentation because %s.", className, filterResult.skipReason));
            return byteCode;
        } else {
            logger.debug(String.format("[instrumenting] '%s'.", className));
        }

        return this.instrumenter.instrument(className, loader, klass);
    }

}
