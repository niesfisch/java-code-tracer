package de.marcelsauer.profiler.instrumenter;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.recorder.Recorder;
import javassist.CtMethod;
import org.apache.log4j.Logger;

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

//    public void instrument(CtMethod declaredMethod) throws Exception {
//        String methodNameLong = declaredMethod.getLongName();
//
//        boolean startRecorder = isRecorderStartMethod(declaredMethod);
//
//        if (startRecorder) {
//            logger.debug("[will start recorder for] method '" + declaredMethod.getLongName() + "'");
//            declaredMethod.insertBefore("de.marcelsauer.profiler.recorder.Recorder.switchOn();" + "de.marcelsauer.profiler.recorder.Recorder.start(\"" + methodNameLong + "\");");
//            declaredMethod.insertAfter("de.marcelsauer.profiler.recorder.Recorder.stop();" + "de.marcelsauer.profiler.recorder.Recorder.switchOff();", true);
//        } else {
//            logger.debug("[will record] method '" + declaredMethod.getLongName() + "'");
//            declaredMethod.insertBefore("de.marcelsauer.profiler.recorder.Recorder.start(\"" + methodNameLong + "\");");
//            declaredMethod.insertAfter("de.marcelsauer.profiler.recorder.Recorder.stop();", true);
//        }
//    }
//
//    private boolean isRecorderStartMethod(CtMethod declaredMethod) throws NotFoundException {
//        for (CtClass interf : declaredMethod.getDeclaringClass().getInterfaces()) {
//            if (config.getRecorderStartInterfaces().contains(interf.getName())) {
//                return true;
//            }
//        }
//        return false;
//    }
}
