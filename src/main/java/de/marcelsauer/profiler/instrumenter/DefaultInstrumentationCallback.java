package de.marcelsauer.profiler.instrumenter;

import javassist.CtMethod;
import org.apache.log4j.Logger;

/**
 * @author msauer
 */
public class DefaultInstrumentationCallback implements Instrumenter.InstrumentationCallback {

    private static final Logger logger = Logger.getLogger(DefaultInstrumentationCallback.class);
    private static final String RECORDER_CLASS_NAME = "de.marcelsauer.profiler.recorder.Recorder";

    public boolean instrument(CtMethod declaredMethod) {
        if (declaredMethod.getMethodInfo().getCodeAttribute() == null) {
            logger.debug("[skipping empty method] '" + declaredMethod.getLongName() + "'");
            return false;
        }

        String methodNameLong = escapeForJavaString(declaredMethod.getLongName());

        logger.debug("[will record] method '" + declaredMethod.getLongName() + "'");
        try {
            declaredMethod.insertBefore(createStartSnippet(methodNameLong));
            declaredMethod.insertAfter(createStopSnippet(), true);
            return true;
        } catch (Exception e) {
            logger.warn(String.format("could not instrument method %s: %s", declaredMethod.getLongName(), e.getMessage()));
            return false;
        }
    }

    private String createStartSnippet(String methodNameLong) {
        return "{"
            + "try {"
            + "ClassLoader jctLoader = ClassLoader.getSystemClassLoader();"
            + "Class jctRecorderClass = Class.forName(\"" + RECORDER_CLASS_NAME + "\", true, jctLoader);"
            + "java.lang.reflect.Method jctStartMethod = jctRecorderClass.getMethod(\"start\", new Class[]{String.class});"
            + "jctStartMethod.invoke(null, new Object[]{\"" + methodNameLong + "\"});"
            + "} catch (Throwable jctIgnored) {"
            + "}"
            + "}";
    }

    private String createStopSnippet() {
        return "{"
            + "try {"
            + "ClassLoader jctLoader = ClassLoader.getSystemClassLoader();"
            + "Class jctRecorderClass = Class.forName(\"" + RECORDER_CLASS_NAME + "\", true, jctLoader);"
            + "java.lang.reflect.Method jctStopMethod = jctRecorderClass.getMethod(\"stop\", new Class[0]);"
            + "jctStopMethod.invoke(null, new Object[0]);"
            + "} catch (Throwable jctIgnored) {"
            + "}"
            + "}";
    }

    private String escapeForJavaString(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
