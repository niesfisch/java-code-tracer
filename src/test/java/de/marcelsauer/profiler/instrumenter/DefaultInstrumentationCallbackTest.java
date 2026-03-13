package de.marcelsauer.profiler.instrumenter;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultInstrumentationCallbackTest {

    @Test
    public void thatInstrumentedMethodWithReflectiveRecorderCallsCompilesToBytecode() throws Exception {
        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.makeClass("de.marcelsauer.profiler.instrumenter.GeneratedInstrumentationTarget");
        CtMethod ctMethod = CtMethod.make("public void sample() { }", ctClass);
        ctClass.addMethod(ctMethod);

        boolean instrumented = new DefaultInstrumentationCallback().instrument(ctMethod);

        byte[] bytecode = ctClass.toBytecode();
        ctClass.detach();

        assertNotNull(bytecode);
        assertTrue(instrumented);
    }

    @Test
    public void thatAbstractMethodWithoutBodyIsSkipped() throws Exception {
        ClassPool classPool = new ClassPool(true);
        CtClass ctClass = classPool.makeClass("de.marcelsauer.profiler.instrumenter.GeneratedAbstractInstrumentationTarget");
        ctClass.setModifiers(Modifier.ABSTRACT | Modifier.PUBLIC);
        CtMethod ctMethod = CtMethod.make("public abstract void sample();", ctClass);
        ctClass.addMethod(ctMethod);

        boolean instrumented = new DefaultInstrumentationCallback().instrument(ctMethod);

        ctClass.detach();

        assertFalse(instrumented);
    }
}

