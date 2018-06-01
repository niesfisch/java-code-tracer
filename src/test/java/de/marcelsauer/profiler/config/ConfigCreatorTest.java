package de.marcelsauer.profiler.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ConfigCreatorTest {

    @Test
    public void testThatNoIncludesOrExcludesDoesntBreak() {
        Config.initDefaultFromYamlFile();
    }

    @Test
    public void testThatPackagesAreInConfig() {
        //given

        //when
        Config configuration = Config.initDefaultFromYamlFile();

        //then
        assertNotNull(configuration);
        assertEquals(0, configuration.classes.included.size());
        assertEquals(0, configuration.classes.excluded.size());
    }

}
