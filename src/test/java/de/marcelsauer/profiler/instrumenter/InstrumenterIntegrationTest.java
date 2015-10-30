package de.marcelsauer.profiler.instrumenter;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InstrumenterIntegrationTest {

    @Test
    public void todoIntegrationTest() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
//        // given
//        assertFalse(ClassWithMethods.instrumented); // todo check if this makes sense
//
//        Instrumenter instrumenter = new Instrumenter(new Instrumenter.InstrumentationCallback() {
//            @Override
//            public void instrument(CtMethod declaredMethod) throws ClassNotFoundException, CannotCompileException {
//                declaredMethod.insertBefore("instrumented = true;");
//            }
//        });
//        String className = "de/marcelsauer/profiler/ClassWithMethods";
//
//        // when
//        byte[] byteCode = instrumenter.instrument(className);
//
//        // then
//        assertNotNull(byteCode);
//        assertMethodWasInstrumented(byteCode);
    }

//    private void assertMethodWasInstrumented(byte[] byteCode) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
//        Class c = new ByteArrayClassLoader(byteCode).findClass("de.marcelsauer.profiler.samplefiles.ClassWithMethods");
//        Object instance = c.newInstance();
//        Method method = c.getMethod("a");
//        method.invoke(instance);
//        assertTrue(c.getField("instrumented").getBoolean(instance));
//    }
//
//    public class ByteArrayClassLoader extends ClassLoader {
//
//        private final byte[] ba;
//
//        public ByteArrayClassLoader(byte[] ba) {
//            this.ba = ba;
//        }
//
//        public Class findClass(String name) throws ClassNotFoundException {
//            try {
//                return defineClass(name, ba, 0, ba.length);
//            } catch (Exception e) {
//                return super.findClass(name);
//            }
//        }
//
//    }

}
