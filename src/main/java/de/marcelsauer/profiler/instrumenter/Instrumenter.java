package de.marcelsauer.profiler.instrumenter;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.recorder.Statistics;
import de.marcelsauer.profiler.util.Util;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

/**
 * @author msauer
 */
public class Instrumenter {

    private static final Logger logger = Logger.getLogger(Instrumenter.class);
    private final InstrumentationCallback callback;
    private boolean done = false;

    public Instrumenter(InstrumentationCallback callback) {
        this.callback = callback;
    }

    public interface InstrumentationCallback {
        void instrument(CtMethod declaredMethod);
    }

    public byte[] instrument(String className, ClassLoader loader, Class klass) {
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
                    this.callback.instrument(declaredMethod);
                    Statistics.addInstrumentedMethod(declaredMethod.getLongName());
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
        ClassPool cp = ClassPool.getDefault();
        if (!done) {
            cp.insertClassPath(new LoaderClassPath(loader));
            done = true;
        }
        return cp;
    }

}
