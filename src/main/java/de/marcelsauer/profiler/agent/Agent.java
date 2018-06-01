package de.marcelsauer.profiler.agent;

import java.lang.instrument.Instrumentation;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.processor.inmemory.server.Server;
import de.marcelsauer.profiler.transformer.Transformer;

/**
 * start with
 * <p/>
 * -javaagent: /path-to-jar/code-tracer/target/javaagent-sample-1.0-SNAPSHOT-jar-with-dependencies.jar
 * -Dconfig=test-config.yaml (optional)
 * -Dtracer.server.port=9002 (default=9001)
 * *
 * https://web.archive.org/web/20141014195801/http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/
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

        logger.info("registering instrumentation transformer");
        inst.addTransformer(new Transformer(Config.get()));

        // new Server().start();
    }
}
