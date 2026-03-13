package de.marcelsauer.profiler.agent;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.transformer.Transformer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * start with
 * <p/>
 * -javaagent: /path-to-jar/code-tracer/target/javaagent-sample-1.0-SNAPSHOT-jar-with-dependencies.jar
 * -Dconfig=test-config.yaml (optional)
 * -Dtracer.server.port=9002 (default=9001)
 * *
 * <a href="https://web.archive.org/web/20141014195801/http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/">...</a>
 *
 * @author msauer
 */
public class Agent {

    private static final Logger logger = Logger.getLogger(Agent.class);

    public static Instrumentation INSTRUMENTATION;

    public static void agentmain(String args, Instrumentation inst) {
        init(inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        init(inst);
    }

    private static void init(Instrumentation inst) {
        INSTRUMENTATION = inst;

        appendAgentJarToClassLoaderSearch(inst);

        logger.info("registering instrumentation transformer");
        inst.addTransformer(new Transformer(Config.get()));

        // new Server().start();
    }

    static void appendAgentJarToClassLoaderSearch(Instrumentation inst) {
        File agentJar = resolveCurrentAgentJar();
        if (agentJar == null) {
            logger.warn("could not resolve current agent jar; runtime visibility for injected recorder calls may be limited");
            return;
        }

        try {
            JarFile jarFile = new JarFile(agentJar);
            inst.appendToSystemClassLoaderSearch(jarFile);
            logger.info("appended agent jar to system classloader search: " + agentJar.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("could not append agent jar to classloader search: " + agentJar.getAbsolutePath(), e);
        }
    }

    static void appendBootstrapBridgeToBootstrapClassLoaderSearch(Instrumentation inst) {
        try {
            File bootstrapJar = createBootstrapBridgeJar();
            if (bootstrapJar == null) {
                logger.warn("could not create bootstrap bridge jar; injected classes may not see the recorder bridge");
                return;
            }
            inst.appendToBootstrapClassLoaderSearch(new JarFile(bootstrapJar));
            logger.info("appended bootstrap recorder bridge jar: " + bootstrapJar.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("could not append bootstrap recorder bridge to classloader search", e);
        }
    }

    static File createBootstrapBridgeJar() {
        String resourceName = "de/marcelsauer/profiler/bridge/BootstrapRecorderBridge.class";
        InputStream bridgeClassInput = Agent.class.getClassLoader().getResourceAsStream(resourceName);
        if (bridgeClassInput == null) {
            return null;
        }

        try (InputStream input = bridgeClassInput) {
            File jarFile = File.createTempFile("jct-bootstrap-bridge", ".jar");
            jarFile.deleteOnExit();
            try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile.toPath()))) {
                JarEntry jarEntry = new JarEntry(resourceName);
                jarOutputStream.putNextEntry(jarEntry);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    jarOutputStream.write(buffer, 0, bytesRead);
                }
                jarOutputStream.closeEntry();
            }
            return jarFile;
        } catch (IOException e) {
            throw new RuntimeException("could not create bootstrap bridge jar", e);
        }
    }

    static File resolveCurrentAgentJar() {
        try {
            CodeSource codeSource = Agent.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return null;
            }

            URL location = codeSource.getLocation();
            if (location == null) {
                return null;
            }

            File locationFile = new File(location.toURI());
            if (!locationFile.isFile()) {
                return null;
            }
            if (!locationFile.getName().endsWith(".jar")) {
                return null;
            }
            return locationFile;
        } catch (URISyntaxException e) {
            throw new RuntimeException("could not resolve current agent jar", e);
        }
    }
}
