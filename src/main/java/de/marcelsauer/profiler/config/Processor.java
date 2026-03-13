package de.marcelsauer.profiler.config;

/**
 * yaml bean
 *
 * @author msauer
 */
public class Processor {
    public String fullQualifiedClass;

    public String udpHost = "localhost";
    public int udpPort = 9999;

    public String tcpHost = "localhost";
    public int tcpPort = 9999;
    public int tcpConnectTimeoutMillis = 1000;
    public int tcpReconnectDelayMillis = 1000;

    public String stackFolderName;
}
