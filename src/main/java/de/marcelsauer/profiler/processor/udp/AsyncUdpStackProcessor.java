package de.marcelsauer.profiler.processor.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;

import org.apache.log4j.Logger;

import de.marcelsauer.profiler.config.Config;
import de.marcelsauer.profiler.processor.AbstractAsyncStackProcessor;
import de.marcelsauer.profiler.processor.RecordingEvent;

/**
 * sends data to configured udp socket
 */
public class AsyncUdpStackProcessor extends AbstractAsyncStackProcessor {

    private static final Logger logger = Logger.getLogger(AsyncUdpStackProcessor.class);

    private DatagramSocket socket;
    private InetAddress address;

    @Override
    protected void doStart() {
        initSocketAndAddress();
        logger.info(String.format("started udp stack processor on %s:%d", Config.get().processor.udpHost, Config.get().processor.udpPort));
    }

    @Override
    protected void doStop() {
        socket.close();
    }

    @Override
    protected void doProcess(Collection<RecordingEvent> snapshots) {
        for (RecordingEvent event : snapshots) {
            String json = event.asJson();
            byte[] buf = new byte[0];
            try {
                buf = json.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Config.get().processor.udpPort);
                socket.send(packet);
            } catch (IOException e) {
                // UDP has max length of 64k message ...
                throw new RuntimeException("could not send message. discarding. length was: " + buf.length, e);
            }
        }
    }


    private void initSocketAndAddress() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(Config.get().processor.udpHost);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
