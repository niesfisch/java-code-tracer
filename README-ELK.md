# JCT with ELK (Local Docker Setup)

This guide shows how to run Elasticsearch, Logstash, and Kibana locally for JCT UDP or TCP events.
It uses the files already in this repository:

- `docker-compose.yml`
- `logstash.conf`

For stack analysis patterns (KQL, Dev Tools queries, trends, top classes/methods), see:

- `README-analysis-ELK.md`

## Table of Contents

- [What You Get](#what-you-get)
- [1) Start the ELK Stack](#1-start-the-elk-stack)
- [2) Open Kibana UI](#2-open-kibana-ui)
- [3) Send Data from JCT via UDP or TCP](#3-send-data-from-jct-via-udp-or-tcp)
- [4) Configure Kibana Data View](#4-configure-kibana-data-view)
- [5) Explore Logs in Discover](#5-explore-logs-in-discover)
- [Quick UDP Smoke Test (Without JCT)](#quick-udp-smoke-test-without-jct)
- [Quick TCP Smoke Test (Without JCT)](#quick-tcp-smoke-test-without-jct)
- [Troubleshooting](#troubleshooting)
- [Stop the Stack](#stop-the-stack)

## What You Get

- Elasticsearch on `http://localhost:9200`
- Logstash UDP input on `localhost:9999`
- Logstash TCP input on `localhost:9999`
- Kibana UI on `http://localhost:5601`
- Index pattern written by Logstash: `jct-events-%{+YYYY.MM.dd}`

## 1) Start the ELK Stack

From the repository root:

```bash
docker compose up -d
docker compose ps
```

Optional health checks:

```bash
curl -fsS http://localhost:9200 >/dev/null && echo "Elasticsearch is up"
curl -fsS http://localhost:5601 >/dev/null && echo "Kibana is up"
curl -fsS http://localhost:9600 >/dev/null && echo "Logstash API is up"
```

## 2) Open Kibana UI

Open:

- `http://localhost:5601`

If Kibana is still booting, wait a few seconds and refresh.

## 3) Send Data from JCT via UDP or TCP

Use a UDP processor config such as:

- `src/test/resources/integration/test-config-asyncudp.yaml`
- or `doc/config-sample-helloworld-udp.yaml`

Or use TCP processor config such as:

- `src/test/resources/integration/test-config-asynctcp.yaml`
- or `doc/config-sample-helloworld-tcp.yaml`

The required processor fields are:

- `processor.fullQualifiedClass: de.marcelsauer.profiler.processor.udp.AsyncUdpStackProcessor`
- `processor.udpHost: localhost`
- `processor.udpPort: 9999`

For TCP, use:

- `processor.fullQualifiedClass: de.marcelsauer.profiler.processor.tcp.AsyncTcpStackProcessor`
- `processor.tcpHost: localhost`
- `processor.tcpPort: 9999`
- `processor.tcpConnectTimeoutMillis: 1000`
- `processor.tcpReconnectDelayMillis: 1000`

Example run (hello world sample):

```bash
java \
  -javaagent:"${PWD}/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  -Djct.loglevel=INFO \
  -Djct.config="${PWD}/doc/config-sample-helloworld-udp.yaml" \
  -Djct.logDir=/tmp/jct \
  -noverify \
  -jar "${PWD}/doc/helloworld-loop.jar"
```

To run over TCP, switch config file to:

- `-Djct.config="${PWD}/doc/config-sample-helloworld-tcp.yaml"`

## 4) Configure Kibana Data View

In Kibana:

1. Go to `Stack Management -> Data Views`.
2. Click `Create data view`.
3. Name: `JCT Events` (or any name).
4. Index pattern: `jct-events-*`.
5. Time field:
   - If a timestamp field is available, select it.
   - Otherwise choose the option without a time filter.

## 5) Explore Logs in Discover

1. Open `Discover`.
2. Select your data view (`jct-events-*`).
3. Add useful columns, for example:
   - `stack`
   - `timestampMillis`
4. Use KQL filters to narrow results.

Examples:

- `stack:*InSubA*`
- `stack:*integration.package*`

## Quick UDP Smoke Test (Without JCT)

Send one JSON event directly to Logstash:

```bash
echo '{"service":"jct","msg":"hello from udp","level":"INFO"}' | nc -u -w1 localhost 9999
```

Then verify Elasticsearch has data:

```bash
curl -s "http://localhost:9200/jct-events-*/_search?pretty"
```

## Quick TCP Smoke Test (Without JCT)

Send one JSON line to Logstash over TCP:

```bash
echo '{"service":"jct","msg":"hello from tcp","level":"INFO"}' | nc -w1 localhost 9999
```

Then verify Elasticsearch has data:

```bash
curl -s "http://localhost:9200/jct-events-*/_search?pretty"
```

## Troubleshooting

- No indices in Elasticsearch:
  - Check Logstash logs: `docker compose logs --no-color logstash | tail -n 200`
  - Confirm UDP/TCP target is `localhost:9999` in your JCT config.
- Kibana shows no documents:
  - Confirm data view matches `jct-events-*`.
  - Expand the time range in Discover.
- Port already in use:
  - Stop conflicting services or change host port mappings in `docker-compose.yml`.

## Stop the Stack

```bash
docker compose down
```

## Start from Scratch (wipe all data)

Remove containers **and** named volumes (`elasticsearch-data`, `kibana-data`), then start fresh:

```bash
docker compose down -v
docker compose up -d
```

Wait ~30 seconds for health checks, then send a smoke event to recreate the `jct-events-*` index:

```bash
printf '{"service":"jct","msg":"smoke","stack":["com.example.Foo.bar()"]}\n' | nc -u -w1 127.0.0.1 9999
```

Verify the index exists:

```bash
curl -s "http://localhost:9200/_cat/indices/jct-events-*?v"
```

To also remove locally built/pulled images:

```bash
docker compose down -v --rmi local
docker compose up -d
```
