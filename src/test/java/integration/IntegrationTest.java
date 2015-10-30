package integration;

import de.marcelsauer.profiler.collect.Collector;
import integration.package1.A;
import integration.package1.B;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * start with
 * <p/>
 * -javaagent: /path-to-jar/code-tracer/target/javaagent-sample-1.0-SNAPSHOT-jar-with-dependencies.jar
 * -Dconfig=test-config.yaml
 * -Dtracer.server.port=9002
 *
 * @author msauer
 */
public class IntegrationTest {


    @Test
    public void thatStackIsInstrumented() throws InterruptedException, FileNotFoundException {
        // given
        A a = new A();

        // when
        a.a();
        a.a();
        a.a();

        B b = new B();
        b.b();

        Thread.sleep(10000000);

        // meanwhile ....
        // curl localhost:9001/status/
        // curl localhost:9001/purge/

        System.out.println("done sleeping .....");

        // then
        assertEquals(2, Collector.getCollectedStacks().size());
        Collection<Integer> values = Collector.getCollectedStacks().values();
        assertTrue(values.contains(1));
        assertTrue(values.contains(3));
    }

}
