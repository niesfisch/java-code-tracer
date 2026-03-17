# Java Code Tracer (JCT)

JCT is a Java agent that records real method call stacks while your application is running.

If you work in a legacy app and ask things like "Can we remove this?" or "Is this code path still hit in production traffic?", JCT gives you hard runtime evidence instead of guesses.

## Table of Contents

- [Why This Is Useful in Legacy Systems](#why-this-is-useful-in-legacy-systems)
- [What JCT Does at Runtime](#what-jct-does-at-runtime)
- [Quick Start in 3 Steps](#quick-start-in-3-steps)
- [Recommended Local Workflow: ELK](#recommended-local-workflow-elk)
- [Project Status](#project-status)
- [Java Version](#java-version)
- [Build](#build)
- [Configure](#configure)
- [Run an Application with JCT](#run-an-application-with-jct)
- [Available Processors](#available-processors)
- [Stack Volume Control (All vs New Stacks)](#stack-volume-control-all-vs-new-stacks)
- [Message Format](#message-format)
- [Logging](#logging)
- [Hello World Walkthrough](#hello-world-walkthrough)
- [Tools](#tools)
- [ELK Integration Guide](#elk-integration-guide)
- [Similar Projects](#similar-projects)
- [IntelliJ JVM Options Example](#intellij-jvm-options-example)
- [License](#license)

## Why This Is Useful in Legacy Systems

In older monoliths and large shared platforms, static code search is usually not enough.

- Feature flags, reflection, and framework magic hide real call paths
- "Unused" code often turns out to be used by one weird but critical flow
- Refactoring without runtime traces is risky and slow

JCT helps you reduce that risk by showing what was actually executed.

## What JCT Does at Runtime

When you attach JCT via `-javaagent`, it does this:

1. Instruments methods that match your include/exclude config
2. Captures call stacks during real execution
3. Sends stack events to a processor (file, UDP or TCP)
4. Lets you inspect those events to validate or reject assumptions

This is especially useful before deleting legacy code, splitting modules, or tightening APIs.

## Quick Start in 3 Steps

If you just want first results quickly:

1. Build JCT
2. Start your app with `-javaagent` and a config
3. Inspect generated events (file output or ELK stack)

Minimum commands:

```bash
mvn clean package
mkdir -p "$HOME/.jct"
cp doc/config-sample-file.yaml "$HOME/.jct/config-sample-file.yaml"
java \
  -javaagent:"${PWD}/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  -Djct.config="$HOME/.jct/config-sample-file.yaml" \
  -Djct.logDir=/tmp/jct \
  -noverify \
  -jar /path/to/your-application.jar
```

Then check `/tmp/jct` logs and your configured processor output.

## Recommended Local Workflow: ELK

For local experimentation, the recommended setup is:

```
  -javaagent:jct.jar            docker compose up
  +-------------------+      +-----------------------------------+
  | App + JCT Agent   |      | Logstash :9999                   |
  | Recorder/Processor|----->|   -> Elasticsearch (jct-events-*)|
  +-------------------+      |   -> Kibana :5601                |
                             +-----------------------------------+
```

This gives you a fast feedback loop: run traffic, query traces, validate code paths.

Start here:

- [README-ELK.md](README-ELK.md)

## Project Status

This project targets Java 8 bytecode and is currently focused on practical runtime tracing for legacy and monolithic applications.

## Java Version

> **Minimum: Java 8**

JCT is compiled against Java 8 (`-source 8 -target 8`) and intentionally uses no APIs beyond that level.
This is a deliberate choice — the primary target is legacy and monolithic systems that are often stuck on older JVMs.

It runs fine on newer JVMs (11, 17, 21, …) without any changes.

## Build

```bash
mvn clean package
```

The distributable agent jar is created at:

- `target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Configure

Create a local config file:

```bash
mkdir -p "$HOME/.jct"
cp doc/config-sample-file.yaml "$HOME/.jct/config-sample-file.yaml"
```

Notes:

- Default config is loaded from `src/main/resources/META-INF/config.yaml`
- If `-Djct.config=...` is set, custom config is merged with default config

## Run an Application with JCT

```bash
java \
  -javaagent:"/path/to/java-code-tracer/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  -Djct.loglevel=INFO \
  -Djct.config="$HOME/.jct/config-sample-file.yaml" \
  -Djct.logDir=/tmp/jct \
  -noverify \
  -jar /path/to/your-application.jar
```

## Available Processors

JCT currently ships with three output processors.

- `de.marcelsauer.profiler.processor.file.AsyncFileWritingStackProcessor`
  - Writes one JSON event per line into a daily log file (`jct_yyyy_dd_MM.log`) in `processor.stackFolderName`
  - Best when you want the simplest setup, local debugging, or offline analysis
  - Trade-off: local disk I/O and file handling are on you
  - Hint: captured traces can get very large on busy systems, so filesystem usage can grow quickly
  - Sample configs: `doc/config-sample-file.yaml`, `src/test/resources/integration/test-config-asyncfile.yaml`

- `de.marcelsauer.profiler.processor.udp.AsyncUdpStackProcessor`
  - Sends each JSON event as a UDP datagram to `processor.udpHost:processor.udpPort`
  - Best for low overhead streaming to Logstash when occasional loss is acceptable
  - Trade-off: no delivery guarantee; oversized/failed sends are discarded
  - Sample configs: `doc/config-sample-helloworld-udp.yaml`, `src/test/resources/integration/test-config-asyncudp.yaml`

- `de.marcelsauer.profiler.processor.tcp.AsyncTcpStackProcessor`
  - Sends one JSON event per line over TCP to `processor.tcpHost:processor.tcpPort`
  - Reconnects automatically using `tcpConnectTimeoutMillis` and `tcpReconnectDelayMillis`
  - Best when you want stronger delivery behavior than UDP for central ingestion
  - Trade-off: network/backpressure issues can still cause dropped events after failed writes
  - Sample configs: `doc/config-sample-helloworld-tcp.yaml`, `src/test/resources/integration/test-config-asynctcp.yaml`

Quick chooser:

- Pick `file` if you want easiest first success and local evidence fast
- Pick `udp` if you optimize for throughput and can tolerate some loss
- Pick `tcp` if you want better transport reliability for ELK pipelines

## Stack Volume Control (All vs New Stacks)

On busy systems, writing every single captured stack can create huge event volume.
If your main question is only "Was this path hit at least once?", you can report only new stacks.

Processor flags:

- `processor.reportAllStacks`
  - `true` (default): report every captured event
  - `false`: report only first-seen stack hashes within the current dedup window
- `processor.stackHashResetIntervalMillis`
  - Periodically clears remembered stack hashes to cap memory usage in long-running JVMs
  - In dedup mode (`reportAllStacks: false`): if missing, JCT logs a warning and uses `30000` (30 seconds)

Example: report only new stacks (good for high-traffic legacy systems)

```yaml
processor:
  fullQualifiedClass: de.marcelsauer.profiler.processor.udp.AsyncUdpStackProcessor
  udpHost: localhost
  udpPort: 9999
  reportAllStacks: false
  stackHashResetIntervalMillis: 300000
```

Example: report all stacks (full event stream)

```yaml
processor:
  fullQualifiedClass: de.marcelsauer.profiler.processor.file.AsyncFileWritingStackProcessor
  stackFolderName: /tmp/stacks/
  reportAllStacks: true
```

## Message Format

```json
{
  "stack": [
    "de.example.Service.doWork()",
    "de.example.Repository.findById()"
  ],
  "timestampMillis": "1528120883697"
}
```

- `timestampMillis`: timestamp in milliseconds when the recorded stack entry started
- `stack`: ordered stack frames from entry to exit point

## Logging

JCT writes logs to the directory configured with `-Djct.logDir`.

For more instrumentation details, increase log level:

```bash
-Djct.loglevel=DEBUG
```

## Hello World Walkthrough

Use the sample loop jar in `doc/helloworld-loop.jar`:

```bash
java \
  -javaagent:"${PWD}/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  -Djct.loglevel=INFO \
  -Djct.config="${PWD}/doc/config-sample-helloworld-file.yaml" \
  -Djct.logDir=/tmp/jct \
  -noverify \
  -jar "${PWD}/doc/helloworld-loop.jar"
```

Check agent logs:

```bash
cat /tmp/jct/jct_agent.log
```

## Tools

### Stack Formatter (`tools/format_stack.py`)

Pretty-prints a raw JCT `stack` array into an aligned, human-readable call sequence.
Consecutive calls to the same class are grouped — package names are abbreviated.

Requires Python 3.9+, no dependencies.

```bash
# pipe the bracket string directly
echo '[a.b.Foo.bar(), a.b.Foo.baz()]' | python3 tools/format_stack.py

# from a file
python3 tools/format_stack.py stack.txt

# grab from clipboard (Linux)
xclip -o | python3 tools/format_stack.py
```

Example output:

```
   #  package        class                  method
  ──────────────────────────────────────────────────────
   1  o.b.s.u.ldap   LdapConnectionFactory  .initialize(LdapConnectionConfigurationDTO)
   2                                        .connect()
   3  o.b.s.u.ldap   LdapConnectionConfigurationDTO  .getLdapServer1()
  ...
```

## ELK Integration Guide

For local Elasticsearch + Logstash + Kibana setup (Docker), UI access, data view setup, and log exploration, see the dedicated guide:

- [README-ELK.md](README-ELK.md)
- [README-analysis-ELK.md](README-ELK-analysys.md)

## Similar Projects

- [Datadog dd-trace-java](https://github.com/DataDog/dd-trace-java/)
- [New Relic](https://newrelic.com/)
- [java-callgraph](https://github.com/gousiosg/java-callgraph)

## IntelliJ JVM Options Example

Use the following IntelliJ Run/Debug VM options example when attaching JCT as a Java agent:

![IntelliJ IDEA VM options example](doc/intellij_idea_vm_options.png)

## License

[MIT](LICENSE.txt)

