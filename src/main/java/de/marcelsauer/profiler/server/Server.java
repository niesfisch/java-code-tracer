/*
 * Copyright: EliteMedianet GmbH, Hamburg
 */
package de.marcelsauer.profiler.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.marcelsauer.profiler.collect.Collector;
import de.marcelsauer.profiler.collect.Statistics;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author msauer
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private HttpServer httpServer;
    private Date startTime;

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    public void start() {
        startTime = new Date();

        int port = 9001;
        String p = System.getProperty("tracer.server.port");
        if (p != null && !"".equals(p.trim())) {
            port = Integer.parseInt(p);
        }

        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.httpServer.createContext("/status/", createStatusHandler());
        this.httpServer.createContext("/purge/", createPurgeHandler());
        this.httpServer.createContext("/stacks/", createStackHandler());
        this.httpServer.start();
        this.addShutdownHook(this);
        LOGGER.info(String.format("started health status server on port %d%n", port));
    }

    private class Stats {

        public final int uniqueStacks;
        public final Date started;
        public final long size;
        public final Set<String> instrumentedMethods;
        public final Set<String> instrumentedClasses;
        public final int numberOfMethodsInstrumented;
        public final int numberOfClassesInstrumented;

        private Stats(int uniqueStacks, Date started, long size, Set<String> instrumentedMethods, Set<String> instrumentedClasses) {
            this.uniqueStacks = uniqueStacks;
            this.started = started;
            this.size = size;
            this.instrumentedMethods = instrumentedMethods;
            this.instrumentedClasses = instrumentedClasses;
            this.numberOfMethodsInstrumented = this.instrumentedMethods.size();
            this.numberOfClassesInstrumented = this.instrumentedClasses.size();

        }

        @Override
        public String toString() {
            return String.format(
                "{\n" +
                    "   \"uniqueStacks\":%d,\n" +
                    "   \"started\":\"%s\",\n" +
                    "   \"stackSizeInBytes\":%d,\n" +
                    "   \"numberOfMethodsInstrumented\":%d,\n" +
                    "   \"numberOfClassesInstrumented\":%d\n" +
                    "}",
                uniqueStacks,
                started,
                size,
                numberOfMethodsInstrumented,
                numberOfClassesInstrumented
            );
        }

    }

    private int size(Map map) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.close();
            int size = baos.size();
            oos = null;
            baos = null;
            return size;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                Map<String, Integer> collectedStacks = Collector.getCollectedStacks();

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

    private HttpHandler createStatusHandler() {
        return new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                addNoCache(exchange);
                addJson(exchange);
                exchange.sendResponseHeaders(200, 0);
                Map<String, Integer> collectedStacks = Collector.getCollectedStacks();

                Set<String> instrumentedMethods = Statistics.getInstrumentedMethods();
                Set<String> instrumentedClasses = Statistics.getInstrumentedClasses();
                Stats stats = new Stats(collectedStacks.size(), startTime, size(collectedStacks), instrumentedMethods, instrumentedClasses);

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
                Collector.purgeCollectedStacks();
                exchange.getResponseBody().write("purged".getBytes("UTF-8"));
                exchange.close();
            }
        };
    }

    public void stop() {
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
