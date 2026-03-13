# Java Code Tracer (JCT)

JCT is a Java agent that instruments method calls at runtime and records executed call stacks.
It helps answer one practical question in large systems: "Is this code path still used in real traffic?"

## Table of Contents

- [What JCT Does](#what-jct-does)
- [Recommended Local Workflow: ELK](#recommended-local-workflow-elk)
- [Project Status](#project-status)
- [Build](#build)
- [Configure](#configure)
- [Run an Application with JCT](#run-an-application-with-jct)
- [Available Processors](#available-processors)
- [Message Format](#message-format)
- [Logging](#logging)
- [Hello World Walkthrough](#hello-world-walkthrough)
- [Tools](#tools)
- [ELK Integration Guide](#elk-integration-guide)
- [Similar Projects](#similar-projects)
- [License](#license)

## What JCT Does

- Instruments selected classes and methods via `-javaagent`
- Captures call stacks and timestamps at runtime
- Supports multiple output processors (file and UDP)
- Lets you analyze runtime behavior without changing app code

## Recommended Local Workflow: ELK

For local experimentation, the recommended setup is:

```
  -javaagent:jct.jar          Docker Compose
  ┌─────────────────┐     ┌───────────────────────────────────┐
  │   JCT Agent     │     │  Logstash :9999                   │
  │   Recorder      │────▶│    │                              │
  │   Stack         │     │    ▼                              │
  │   Processor     │     │  Elasticsearch  (jct-events-*)    │
  │  UDP / TCP      │     │    │                              │
  └─────────────────┘     │    ▼                              │
                          │  Kibana :5601                     │
                          └───────────────────────────────────┘
```

This gives you a fast feedback loop with searchable traces and a UI for exploration.

Start here:

- [README-ELK.md](README-ELK.md)

## Project Status

This project targets Java 8 bytecode and is currently focused on practical runtime tracing for legacy and monolithic applications.

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

- `de.marcelsauer.profiler.processor.file.AsyncFileWritingStackProcessor`
  - Example config: `src/test/resources/integration/test-config-asyncfile.yaml`
- `de.marcelsauer.profiler.processor.udp.AsyncUdpStackProcessor`
  - Example config: `src/test/resources/integration/test-config-asyncudp.yaml`
- `de.marcelsauer.profiler.processor.tcp.AsyncTcpStackProcessor`
  - Example config: `src/test/resources/integration/test-config-asynctcp.yaml`

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
  -Djct.config="${PWD}/doc/config-sample-helloworld.yaml" \
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

# For me :)

```bash
GIT_SSH_COMMAND='ssh -i ~/.ssh/niesfisch' git pull
GIT_SSH_COMMAND='ssh -i ~/.ssh/niesfisch' git push
```
