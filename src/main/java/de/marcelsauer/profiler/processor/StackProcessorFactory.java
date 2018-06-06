package de.marcelsauer.profiler.processor;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.processor.inmemory.InMemoryStackProcessor;
import de.marcelsauer.profiler.util.Util;

public class StackProcessorFactory {
    private static final Logger logger = Logger.getLogger(StackProcessorFactory.class);
    private static StackProcessor stackProcessor;

    public static StackProcessor getStackProcessor() {
        if (stackProcessor != null) {
            return stackProcessor;
        }
        String processor = Config.get().processor.fullQualifiedClass;
        if (Util.isEmpty(processor)) {
            stackProcessor = new InMemoryStackProcessor();
        } else {
            try {
                Class<?> pr = Class.forName(processor);
                stackProcessor = (StackProcessor) pr.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("could not create stack processor " + stackProcessor, e);
            }
        }
        logger.info("using stack processor of type: " + stackProcessor.getClass().getName());

        stackProcessor.start();

        addShutdownHook();

        return stackProcessor;
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("jca-shutdown-thread") {
            @Override
            public void run() {
                logger.info("shutting down jca processor " + stackProcessor.getClass().getName());
                stackProcessor.stop();
            }
        });
    }
}
