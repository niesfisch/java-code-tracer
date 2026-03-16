package de.marcelsauer.profiler.config;

/**
 * yaml bean
 *
 * @author msauer
 */
public class Processor {
    public String fullQualifiedClass;

    /**
     * true: report every captured stack event
     * false: report only first-seen stack hashes until periodic reset
     */
    public boolean reportAllStacks = true;

    /**
     * reset interval for remembered stack hashes in dedup mode to cap memory usage
     */
    public long stackHashResetIntervalMillis = 300_000L;

    public String udpHost = "localhost";
    public int udpPort = 9999;

    public String tcpHost = "localhost";
    public int tcpPort = 9999;
    public int tcpConnectTimeoutMillis = 1000;
    public int tcpReconnectDelayMillis = 1000;

    public String stackFolderName;
}
