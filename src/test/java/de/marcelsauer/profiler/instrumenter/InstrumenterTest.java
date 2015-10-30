package de.marcelsauer.profiler.instrumenter;

import de.marcelsauer.profiler.instrumenter.Instrumenter;
import javassist.CannotCompileException;
import javassist.CtMethod;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class InstrumenterTest {

//    @Test
//    public void thatClassWithoutMethodWorks() {
//        // given
//        Instrumenter.InstrumentationCallback callback = mock(Instrumenter.InstrumentationCallback.class);
//        Instrumenter instrumenter = new Instrumenter(callback);
//        String classWithoutMethods = "de/marcelsauer/profiler/samplefiles/ClassWithoutMethods";
//
//        // when
//        byte[] byteCode = instrumenter.instrument(classWithoutMethods);
//
//        // then
//        assertNotNull(byteCode);
//        verifyZeroInteractions(callback);
//    }
//
//    @Test
//    public void thatClassWithMethodWorks() throws Exception {
//        // given
//        Instrumenter.InstrumentationCallback callback = mock(Instrumenter.InstrumentationCallback.class);
//        Instrumenter instrumenter = new Instrumenter(callback);
//        String className = "de/marcelsauer/profiler/samplefiles/ClassWithMethods";
//
//        // when
//        byte[] byteCode = instrumenter.instrument(className);
//
//        // then
//        assertNotNull(byteCode);
//        verify(callback).instrument(any(CtMethod.class));
//    }
}
