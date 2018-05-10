package de.marcelsauer.profiler.instrumenter;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.recorder.Recorder;
import javassist.CtMethod;

/**
 * @author msauer
 */
public class DefaultInstrumentationCallback implements Instrumenter.InstrumentationCallback {

    private static final Logger logger = Logger.getLogger(DefaultInstrumentationCallback.class);
    private final Config config;

    public DefaultInstrumentationCallback(Config config) {
        this.config = config;
    }

    public void instrument(CtMethod declaredMethod) {
        String methodNameLong = declaredMethod.getLongName();

        logger.debug("[will record] method '" + declaredMethod.getLongName() + "'");
        try {
            declaredMethod.insertBefore(String.format("%s.start(\"%s\");", Recorder.class.getName(), methodNameLong));
            declaredMethod.insertAfter(String.format("%s.stop();", Recorder.class.getName()), true);
        } catch (Exception e) {
            logger.warn(String.format("could not instrument method %s: %s", declaredMethod.getLongName(), e.getMessage()));
        }
    }

}
