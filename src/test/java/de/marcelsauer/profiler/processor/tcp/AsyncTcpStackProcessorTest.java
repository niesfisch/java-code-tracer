package de.marcelsauer.profiler.processor.tcp;

import de.marcelsauer.profiler.processor.RecordingEvent;
import de.marcelsauer.profiler.processor.Stack;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AsyncTcpStackProcessorTest {

    @Test
    public void thatTcpProcessorSendsJsonLines() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        CountDownLatch messageReceived = new CountDownLatch(1);
        AtomicReference<String> line = new AtomicReference<>();

        Thread serverThread = new Thread(() -> {
            try (Socket socket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                line.set(reader.readLine());
                messageReceived.countDown();
            } catch (Exception ignored) {
                // ignore in test helper thread
            }
        });
        serverThread.start();

        AsyncTcpStackProcessor processor = new AsyncTcpStackProcessor("127.0.0.1", port, 500, 10);
        processor.start();
        try {
            processor.doProcess(Collections.singletonList(newEvent("de.example.Service.doWork()")));
            assertTrue(messageReceived.await(2, TimeUnit.SECONDS));
            assertNotNull(line.get());
            assertTrue(line.get().contains("de.example.Service.doWork()"));
        } finally {
            processor.stop();
            serverSocket.close();
        }
    }

    @Test
    public void thatTcpConnectFailureReturnsFastWithTimeout() {
        AsyncTcpStackProcessor processor = new AsyncTcpStackProcessor("192.0.2.1", 65000, 150, 10);
        long start = System.currentTimeMillis();

        processor.start();
        try {
            processor.doProcess(Collections.singletonList(newEvent("de.example.Service.doWork()")));
        } finally {
            processor.stop();
        }

        long durationMillis = System.currentTimeMillis() - start;
        assertTrue("connect should fail fast", durationMillis < 2500);
    }

    private RecordingEvent newEvent(String methodName) {
        Stack.StackEntry entry = new Stack.StackEntry(methodName, 1L, 0);
        return new RecordingEvent(new Stack(Collections.singletonList(entry)));
    }
}

