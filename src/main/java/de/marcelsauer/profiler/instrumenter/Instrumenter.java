package de.marcelsauer.profiler.instrumenter;

import de.marcelsauer.profiler.recorder.Statistics;
import de.marcelsauer.profiler.util.Util;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author msauer
 */
public class Instrumenter {

    private static final Logger logger = Logger.getLogger(Instrumenter.class);
    private final InstrumentationCallback callback;
    private final ConcurrentMap<ClassLoader, ClassPool> classPools = new ConcurrentHashMap<>();

    public Instrumenter(InstrumentationCallback callback) {
        this.callback = callback;
    }

    public interface InstrumentationCallback {
        boolean instrument(CtMethod declaredMethod);
    }

    public byte[] instrument(String className, ClassLoader loader) {
        try {

            ClassPool cp = getClassPool(loader);

            CtClass cc = cp.get(Util.fromJVM(className));
            boolean instrument = !cc.isAnnotation() && !cc.isArray() && !cc.isInterface() && !cc.isEnum();

            if (!instrument) {
                return null;
            }

            CtMethod[] declaredMethods = cc.getDeclaredMethods();

            for (CtMethod declaredMethod : declaredMethods) {
                try {
                    boolean instrumented = this.callback.instrument(declaredMethod);
                    if (instrumented) {
                        Statistics.addInstrumentedMethod(declaredMethod.getLongName());
                    }
                } catch (Exception e) {
                    logger.warn(String.format("could not instrument method '%s': %s", className, e.getMessage()));
                    return null;
                }
            }

            byte[] bytes = cc.toBytecode();
            cc.detach();

            Statistics.addInstrumentedClass(className);

            return bytes;
        } catch (Exception ex) {
            logger.warn(String.format("could not instrument class '%s': %s", className, ex.getMessage()));
            return null;
        }
    }

    private ClassPool getClassPool(ClassLoader loader) {
        if (loader == null) {
            return createClassPool(null);
        }
        return classPools.computeIfAbsent(loader, this::createClassPool);
    }

    private ClassPool createClassPool(ClassLoader loader) {
        ClassPool cp = new ClassPool(true);
        if (loader != null) {
            cp.insertClassPath(new LoaderClassPath(loader));
        }
        return cp;
    }

}
