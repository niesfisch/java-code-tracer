package de.marcelsauer.profiler.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * yaml bean
 *
 * @author msauer
 */
public class Config {
    private static final Logger logger = Logger.getLogger(Config.class);
    private static final String DEFAULT_CONFIG_FILE = "META-INF/config.yaml";

    public Classes classes = new Classes();
    public Recorder recorder = new Recorder();
    public Processor processor = new Processor();

    private static Config currentConfig;

    public static Config get() {
        if (currentConfig == null) {
            currentConfig = init();
        }
        return currentConfig;
    }

    private static Config init() {
        Config config = Config.initDefaultFromYamlFile();
        config.merge(Config.loadCustomFromYamlFile());
        logger.info("merged config config" + config);
        return config;
    }

    static Config initDefaultFromYamlFile() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        Config config = (Config) yaml.load(FileUtils.getLocalResource(DEFAULT_CONFIG_FILE));
        logger.info("using default config: " + config);
        return config;
    }

    static Config loadCustomFromYamlFile() {
        String yamlFile = System.getProperty("jct.config");
        if (yamlFile != null && !"".equals(yamlFile.trim())) {

            Yaml yaml = new Yaml(new Constructor(Config.class));
            Config configFromCustomFile = null;
            try {
                InputStream result = new FileInputStream(yamlFile);
                configFromCustomFile = (Config) yaml.load(result);
            } catch (IOException e) {
                throw new RuntimeException("could not load " + yamlFile);
            }

            logger.info(String.format("using custom config from file '%s' with config '%s'", yamlFile, configFromCustomFile));
            return configFromCustomFile;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Config{" +
            "classes=" + classes +
            ", recorder=" + recorder +
            '}';
    }

    private void merge(Config otherConfig) {
        if (otherConfig == null) {
            return;
        }
        mergeClasses(otherConfig);
        mergeRecorder(otherConfig);
        this.processor = otherConfig.processor;
    }

    private void mergeRecorder(Config otherConfig) {
        addNullSafe(otherConfig.recorder.superClasses, this.recorder.superClasses);
        addNullSafe(otherConfig.recorder.interfaces, this.recorder.interfaces);
        addNullSafe(otherConfig.recorder.methodLevelAnnotations, this.recorder.methodLevelAnnotations);
        addNullSafe(otherConfig.recorder.classLevelAnnotations, this.recorder.classLevelAnnotations);
    }

    private void mergeClasses(Config otherConfig) {
        addNullSafe(otherConfig.classes.included, this.classes.included);
        addNullSafe(otherConfig.classes.excluded, this.classes.excluded);
    }

    private void addNullSafe(Set<String> src, Set<String> target) {
        if (src != null) {
            target.addAll(src);
        }
    }

    public boolean isInclusionConfigured() {
        return !this.classes.included.isEmpty();
    }
}
