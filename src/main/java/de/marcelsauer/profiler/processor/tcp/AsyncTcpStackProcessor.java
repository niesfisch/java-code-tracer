package de.marcelsauer.profiler.processor.tcp;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.processor.AbstractAsyncStackProcessor;
import de.marcelsauer.profiler.processor.RecordingEvent;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * sends data to configured tcp socket, one json event per line
 */
public class AsyncTcpStackProcessor extends AbstractAsyncStackProcessor {

    private static final Logger logger = Logger.getLogger(AsyncTcpStackProcessor.class);

    private final String host;
    private final int port;
    private final int connectTimeoutMillis;
    private final int reconnectDelayMillis;

    private Socket socket;
    private BufferedWriter writer;
    private long nextReconnectAttemptMillis;

    public AsyncTcpStackProcessor() {
        this(
            Config.get().processor.tcpHost,
            Config.get().processor.tcpPort,
            Config.get().processor.tcpConnectTimeoutMillis,
            Config.get().processor.tcpReconnectDelayMillis
        );
    }

    AsyncTcpStackProcessor(String host, int port, int connectTimeoutMillis, int reconnectDelayMillis) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMillis = Math.max(1, connectTimeoutMillis);
        this.reconnectDelayMillis = Math.max(1, reconnectDelayMillis);
    }

    @Override
    protected void doStart() {
        logger.info(String.format("started tcp stack processor targeting %s:%d (connect timeout: %dms)", host, port, connectTimeoutMillis));
    }

    @Override
    protected void doStop() {
        closeConnection();
    }

    @Override
    protected void doProcess(Collection<RecordingEvent> snapshots) {
        for (RecordingEvent event : snapshots) {
            sendWithReconnect(event);
        }
    }

    private void sendWithReconnect(RecordingEvent event) {
        if (!ensureConnected()) {
            return;
        }

        if (writeEvent(event)) {
            return;
        }

        closeConnection();

        if (!ensureConnected()) {
            return;
        }

        if (!writeEvent(event)) {
            closeConnection();
        }
    }

    private boolean ensureConnected() {
        if (isConnected()) {
            return true;
        }

        long now = System.currentTimeMillis();
        if (now < nextReconnectAttemptMillis) {
            return false;
        }

        try {
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            newSocket.setTcpNoDelay(true);
            socket = newSocket;
            writer = new BufferedWriter(new OutputStreamWriter(newSocket.getOutputStream(), StandardCharsets.UTF_8));
            nextReconnectAttemptMillis = 0;
            return true;
        } catch (IOException e) {
            nextReconnectAttemptMillis = now + reconnectDelayMillis;
            logger.warn(String.format("could not connect to tcp endpoint %s:%d (timeout: %dms)", host, port, connectTimeoutMillis));
            closeConnection();
            return false;
        }
    }

    private boolean writeEvent(RecordingEvent event) {
        try {
            writer.write(event.asJson());
            writer.newLine();
            writer.flush();
            return true;
        } catch (IOException e) {
            logger.warn(String.format("could not send tcp event to %s:%d. discarding event.", host, port));
            return false;
        }
    }

    private boolean isConnected() {
        return socket != null && writer != null && socket.isConnected() && !socket.isClosed();
    }

    private void closeConnection() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // ignore
            }
            writer = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // ignore
            }
            socket = null;
        }
    }
}

