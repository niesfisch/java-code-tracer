package integration;

import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * assumes you've build the project via <pre>mvn clean package</pre> so that ./target/ contains the agent jar
 */
public class AsyncTcpIntegrationTest extends AbtractAsyncIntegrationTest {

    private static final String JAR_FILE_PATH = "./target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar";

    static {
        System.setProperty("jct.config", "./src/test/resources/integration/test-config-asynctcp.yaml");
        System.setProperty("jct.logDir", "./target");
        AfterVmStartupAgentLoader.loadAgent(JAR_FILE_PATH);
    }

    @Before
    public void startServer() {
        TcpServer.start();
    }

    @Override
    protected void doCustomAssertions(int expectedStackCount) {
        assertEquals(expectedStackCount, TcpServer.counter);
    }

    private static class TcpServer {
        private static volatile int counter = 0;
        private static volatile boolean started = false;

        static synchronized void start() {
            if (started) {
                return;
            }
            started = true;

            Thread thread = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(9999)) {
                    while (true) {
                        try (Socket socket = serverSocket.accept();
                             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                counter++;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
}

