/*
 * Copyright: EliteMedianet GmbH, Hamburg
 */
package de.marcelsauer.profiler.processor.inmemory.server;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.marcelsauer.profiler.processor.inmemory.InMemoryCountingCollector;
import de.marcelsauer.profiler.recorder.Statistics;

/**
 * @author msauer
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private HttpServer httpServer;
    private Date startTime;
    private static final String LOG_DIR = System.getProperty("jct.logDir");

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        startTime = new Date();

        int port = getPort();

        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        configureRouting();

        this.httpServer.start();

        this.addShutdownHook(this);

        LOGGER.info(String.format("started status server on port %d%n", port));
    }

    private void configureRouting() {
        this.httpServer.createContext("/status/", createStatusHandler());
        this.httpServer.createContext("/purge/", createPurgeHandler());
        this.httpServer.createContext("/stacks/", createStackHandler());
        this.httpServer.createContext("/dump/", createDumpHandler());
    }

    private int getPort() {
        int port = 9001;
        String p = System.getProperty("jct.tracer.server.port");
        if (p != null && !"".equals(p.trim())) {
            port = Integer.parseInt(p);
        }
        return port;
    }

    private class Stats {

        final int uniqueStacks;
        final Date started;
        final long size;
        final int instrumentedMethodsCount;
        final int instrumentedClassesCount;

        private Stats(int uniqueStacks, Date started, long size, int instrumentedMethodsCount, int instrumentedClassesCount) {
            this.uniqueStacks = uniqueStacks;
            this.started = started;
            this.size = size;
            this.instrumentedMethodsCount = instrumentedMethodsCount;
            this.instrumentedClassesCount = instrumentedClassesCount;
        }

        @Override
        public String toString() {
            return String.format(
                "{\n" +
                    "   \"uniqueStacks\":%d,\n" +
                    "   \"started\":\"%s\",\n" +
                    "   \"stackSizeInBytes\":%d,\n" +
                    "   \"stackSizeInMBytes\":%d,\n" +
                    "   \"numberOfMethodsInstrumented\":%d,\n" +
                    "   \"numberOfClassesInstrumented\":%d\n" +
                    "}",
                uniqueStacks,
                started,
                size,
                (size / 1024 / 1024),
                instrumentedMethodsCount,
                instrumentedClassesCount
            );
        }

    }

    private void addNoCache(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Cache-Control", "must-revalidate,no-cache,no-store");
    }

    private void addJson(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
    }

    private void addText(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Content-Type", "application/text");
    }

    private HttpHandler createStackHandler() {
        return new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                addNoCache(exchange);
                addText(exchange);
                exchange.sendResponseHeaders(200, 0);
                Map<String, Integer> collectedStacks = InMemoryCountingCollector.getCollectedStacks();

                StringBuilder sb = new StringBuilder();

                Map<String, Integer> sortedByCount = MapUtil.sortByValue(collectedStacks);
                for (String stack : sortedByCount.keySet()) {
                    sb.append(sortedByCount.get(stack) + "x\n" + stack + "\n");
                    sb.append("---------------------------------------------------------------- \n");
                }
                exchange.getResponseBody().write(sb.toString().getBytes("UTF-8"));
                exchange.close();
            }
        };
    }

    private HttpHandler createDumpHandler() {
        return new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                addNoCache(exchange);
                addText(exchange);
                exchange.sendResponseHeaders(201, 0);
                Map<String, Integer> collectedStacks = InMemoryCountingCollector.getCollectedStacks();

                String file = new SimpleDateFormat("yyyy_dd_MM_HHmmss").format(new Date());
                String outFilename = String.format(LOG_DIR + "/jct_%s.txt", file);

                FileWriter writer = null;

                Map<String, Integer> sortedByCount = MapUtil.sortByValue(collectedStacks);
                try {
                    writer = new FileWriter(outFilename);
                    for (String stack : sortedByCount.keySet()) {
                        writer.write(sortedByCount.get(stack) + "x\n" + stack + "\n");
                        writer.write("---------------------------------------------------------------- \n");
                    }
                } finally {
                    writer.flush();
                    writer.close();
                }

                exchange.getResponseBody().write((String.format("written to '%s' consider purging now to free memory ...", outFilename)).getBytes("UTF-8"));
                exchange.close();
            }
        };
    }

    private HttpHandler createStatusHandler() {
        return new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                addNoCache(exchange);
                addJson(exchange);
                exchange.sendResponseHeaders(200, 0);
                Map<String, Integer> collectedStacks = InMemoryCountingCollector.getCollectedStacks();

                Stats stats = new Stats(collectedStacks.size(), startTime, MapUtil.approximateSizeInBytes(collectedStacks), Statistics.getInstrumentedMethodsCount(), Statistics.getInstrumentedClassesCount());

                exchange.getResponseBody().write(stats.toString().getBytes("UTF-8"));
                exchange.close();
            }

        };
    }

    private HttpHandler createPurgeHandler() {
        return new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                addNoCache(exchange);
                addText(exchange);
                exchange.sendResponseHeaders(200, 0);
                InMemoryCountingCollector.purgeCollectedStacks();
                exchange.getResponseBody().write("purged".getBytes("UTF-8"));
                exchange.close();
                LOGGER.info("successfully purged");
            }
        };
    }

    private void stop() {
        this.httpServer.stop(0);
    }

    private void addShutdownHook(final Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread("HealthCheckHttpServerShutdownHook") {
            @Override
            public void run() {
                LOGGER.info("shutdown of server ....");
                server.stop();
            }
        });
    }
}
