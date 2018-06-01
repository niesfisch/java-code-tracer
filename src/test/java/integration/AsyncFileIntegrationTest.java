package integration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;

/**
 * assumes you've build the project via <pre>mvn clean package</pre> so that ./target/ contains the agent jar
 *
 * @author msauer
 */
public class AsyncFileIntegrationTest extends AbtractAsyncIntegrationTest {

    private static final String JAR_FILE_PATH = "./target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar";

    static {
        System.setProperty("jct.config", "./src/test/resources/integration/test-config-asyncfile.yaml");
        System.setProperty("jct.logDir", "./target");
        AfterVmStartupAgentLoader.loadAgent(JAR_FILE_PATH);
    }

    @Before
    public void cleanup() throws IOException {
        Path path = Paths.get(getFilename());
        try {
            Files.delete(path);
        } catch (java.nio.file.NoSuchFileException e) {
            // ignore
        }
    }

    private String getFilename() {
        String fileDateTimePart = new SimpleDateFormat("yyyy_dd_MM").format(new Date());
        return "/tmp/stacks/jct_" + fileDateTimePart + ".log";
    }

    @Override
    protected void doCustomAssertions(int expectedStackCount) {
        try {
            int size = Files.readAllLines(Paths.get(getFilename()), Charset.defaultCharset()).size();
            assertEquals(expectedStackCount, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
