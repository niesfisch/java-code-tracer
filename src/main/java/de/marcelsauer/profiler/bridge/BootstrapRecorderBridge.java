package de.marcelsauer.profiler.bridge;

import java.lang.reflect.Method;

/**
 * Bootstrap-safe bridge used by instrumented classes.
 * It avoids a direct symbolic reference from application bytecode to Recorder.
 */
public final class BootstrapRecorderBridge {

    private static final String RECORDER_CLASS_NAME = "de.marcelsauer.profiler.recorder.Recorder";

    private static volatile Method startMethod;
    private static volatile Method stopMethod;
    private static volatile boolean initializationAttempted;
    private static volatile boolean initializationFailed;
    private static volatile boolean failureLogged;

    private BootstrapRecorderBridge() {
        // utility class
    }

    public static void start(String methodName) {
        Method method = getStartMethod();
        if (method == null) {
            return;
        }
        try {
            method.invoke(null, methodName);
        } catch (Throwable t) {
            logFailureOnce("could not invoke Recorder.start", t);
        }
    }

    public static void stop() {
        Method method = getStopMethod();
        if (method == null) {
            return;
        }
        try {
            method.invoke(null);
        } catch (Throwable t) {
            logFailureOnce("could not invoke Recorder.stop", t);
        }
    }

    private static Method getStartMethod() {
        ensureInitialized();
        return startMethod;
    }

    private static Method getStopMethod() {
        ensureInitialized();
        return stopMethod;
    }

    private static void ensureInitialized() {
        if (initializationAttempted) {
            return;
        }
        synchronized (BootstrapRecorderBridge.class) {
            if (initializationAttempted) {
                return;
            }
            initializationAttempted = true;
            try {
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                Class<?> recorderClass = Class.forName(RECORDER_CLASS_NAME, true, systemClassLoader);
                startMethod = recorderClass.getMethod("start", String.class);
                stopMethod = recorderClass.getMethod("stop");
            } catch (Throwable t) {
                initializationFailed = true;
                logFailureOnce("could not initialize recorder bridge", t);
            }
        }
    }

    private static void logFailureOnce(String message, Throwable t) {
        if (failureLogged) {
            return;
        }
        synchronized (BootstrapRecorderBridge.class) {
            if (failureLogged) {
                return;
            }
            failureLogged = true;
            System.err.println("[jct] " + message + ": " + t);
        }
    }

    static boolean isInitializationFailed() {
        return initializationFailed;
    }
}
