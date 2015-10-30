package de.marcelsauer.profiler.agent;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.server.Server;
import de.marcelsauer.profiler.transformer.Transformer;
import org.apache.log4j.Logger;

import java.lang.instrument.Instrumentation;

/**
 * @author msauer
 */
public class Agent {

    private static final Logger logger = Logger.getLogger(Agent.class);
    private static final String DEFAULT_CONFIG_FILE = "META-INF/config.yaml";

    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("registering instrumentation transformer");


        String configFile = DEFAULT_CONFIG_FILE;
        Config config = Config.createDefaultFromYamlFile(configFile);

        logger.info("using default config: " + config);

        String yamlFile = System.getProperty("config");
        if (yamlFile != null && !"".equals(yamlFile.trim())) {
            Config customConf = Config.createCustomFromYamlFile(yamlFile);
            if (customConf.isInclusionConfigured()) {
                ignoreDefaultInclusion(config);
            }
            logger.info(String.format("using custom config from file '%s' with config '%s'", yamlFile, customConf));
            config.merge(customConf);
        }
        logger.info("merged config config" + config);

        inst.addTransformer(new Transformer(config));
        new Server().start();
    }

    private static void ignoreDefaultInclusion(Config config) {
        config.classes.included.clear();
    }

}
