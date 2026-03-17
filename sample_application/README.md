# Sample Application

This is a tiny Spring Boot sample app that continuously executes a deep method call chain in package `de.marcelsauer.sample`.

## What it does

- Starts via a simple `main` method.
- Runs an endless loop.
- Calls 10 classes (`ClassA` ... `ClassJ`) with depth-based method names.
- Produces stable, repetitive stack traces that are useful for profiler/agent testing.

## Build

```bash
mvn clean package
```

This writes the executable jar to `../doc/java-code-tracer-sample-application.jar`.

## Run

```bash
java -jar ../doc/java-code-tracer-sample-application.jar
```

Stop with `Ctrl+C`.

