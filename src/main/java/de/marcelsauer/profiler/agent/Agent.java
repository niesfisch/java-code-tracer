package de.marcelsauer.profiler.agent;

import java.lang.instrument.Instrumentation;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.server.Server;
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
    private static final String DEFAULT_CONFIG_FILE = "META-INF/config.yaml";

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        init(inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        init(inst);
    }

    private static void init(Instrumentation inst) {
        logger.info("registering instrumentation transformer");
        inst.addTransformer(new Transformer(buildConfig()));
        new Server().start();
    }

    private static Config buildConfig() {
        Config config = Config.createDefaultFromYamlFile(DEFAULT_CONFIG_FILE);
        logger.info("using default config: " + config);

        String yamlFile = System.getProperty("jct.config");
        if (yamlFile != null && !"".equals(yamlFile.trim())) {
            Config customConf = Config.createCustomFromYamlFile(yamlFile);
            if (customConf.isInclusionConfigured()) {
                ignoreDefaultInclusion(config);
            }
            logger.info(String.format("using custom config from file '%s' with config '%s'", yamlFile, customConf));
            config.merge(customConf);
        }
        logger.info("merged config config" + config);
        return config;
    }

    private static void ignoreDefaultInclusion(Config config) {
        config.classes.included.clear();
    }

}
