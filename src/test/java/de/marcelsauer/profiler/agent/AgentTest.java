package de.marcelsauer.profiler.agent;

import org.junit.Test;

import java.io.File;
import java.lang.instrument.Instrumentation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AgentTest {

    @Test
    public void thatResolveCurrentAgentJarReturnsNullOutsideJarRuntime() {
        File currentAgentJar = Agent.resolveCurrentAgentJar();

        assertNull(currentAgentJar);
    }

    @Test
    public void thatAppendAgentJarIsNoopWhenRunningFromClassesDirectory() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        Agent.appendAgentJarToClassLoaderSearch(instrumentation);

        verifyZeroInteractions(instrumentation);
    }

    @Test
    public void thatBootstrapBridgeJarCanBeCreated() {
        File bootstrapBridgeJar = Agent.createBootstrapBridgeJar();

        assertNotNull(bootstrapBridgeJar);
    }
}

