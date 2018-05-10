package de.marcelsauer.profiler.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigCreatorTest {

    @Test
    public void testThatNoIncludesOrExcludesDoesntBreak() {
        Config.createDefaultFromYamlFile("META-INF/config.yaml");
    }

    @Test
    public void testThatPackagesAreInConfig() {
        //given

        //when
        Config configuration = Config.createDefaultFromYamlFile("META-INF/config.yaml");

        //then
        assertNotNull(configuration);
        assertEquals(0, configuration.classes.included.size());
        assertEquals(0, configuration.classes.excluded.size());
    }

}
