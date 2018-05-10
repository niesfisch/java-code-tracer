package integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import org.junit.Test;

import de.marcelsauer.profiler.collect.Collector;
import integration.package1.A;
import integration.package1.B;

/**
 * assumes you've build the project via <pre>mvn clean package</pre> so that ./target/ contains the agent jar
 *
 * @author msauer
 */
public class IntegrationTest {

    private static final String JAR_FILE_PATH = "./target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar";

    static {
        System.setProperty("jct.config", "./src/test/resources/integration/test-config.yaml");
        AfterVmStartupAgentLoader.loadAgent(JAR_FILE_PATH);
    }

    @Test
    public void thatStackIsInstrumented() throws Exception {

        // given
        A a = new A();

        // when
        a.a();
        a.a();
        a.a();

        B b = new B();
        b.b();

        String response = getRequest("http://localhost:9001/status/");

         Thread.sleep(10000000);
//         meanwhile ....
        // curl localhost:9001/status/
        // curl localhost:9001/purge/

        // then
        assertTrue(response.contains("uniqueStacks"));
        assertEquals(2, Collector.getCollectedStacks().size());

        Collection<Integer> values = Collector.getCollectedStacks().values();
        assertTrue(values.contains(1));
        assertTrue(values.contains(3));
    }

    private String getRequest(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

}
