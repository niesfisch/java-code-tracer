package integration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.junit.Before;

/**
 * @author msauer
 */
public class AsyncUdpIntegrationTest extends AbtractAsyncIntegrationTest {

    private static final String JAR_FILE_PATH = "./target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar";

    static {
        System.setProperty("jct.config", "./src/test/resources/integration/test-config-asyncudp.yaml");
        System.setProperty("jct.logDir", "./target");
        AfterVmStartupAgentLoader.loadAgent(JAR_FILE_PATH);
    }

    @Before
    public void startServer() {
        new UdpServer().start();
    }

    @Override
    protected void doCustomAssertions(int expectedStackCount) {
        assertEquals(expectedStackCount, UdpServer.counter);
    }

    private static class UdpServer {
        static int counter = 0;

        void start() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramSocket serverSocket = null;
                    try {
                        serverSocket = new DatagramSocket(9999);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    byte[] receiveData = new byte[1024];
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        try {
                            serverSocket.receive(receivePacket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        counter++;
                    }
                }
            });
            thread.start();
        }
    }

}
