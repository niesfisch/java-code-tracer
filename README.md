[<img src="https://api.travis-ci.org/niesfisch/java-code-tracer.png"/>](http://travis-ci.org/niesfisch/java-code-tracer/builds)

# JCT = Java Code Tracer 

JCT was born out of the idea to collect runtime information from a big monolithic (legacy) application that was running for several years. from time to time there was the same question over and over again:

> do we still need this code? nobody seems to be calling it ....

> but i am still scared to remove it :-(

> maybe the code is called in production?

JCT helps you answering these questions by collecting information about your running application.

# Installing JCT for your application

first clone or download this project

## create config

the config file is the place where you configure which classes and packages of your application should be 'monitored' for calls (aka instrumented). by default nothing will be instrumented as this would produce massive amounts of data. start by choosing a sensible package of your application to  begin with. everything is based on regex(s), so there should be a lot of freedom for you. see the config-sample.yml that you'll be copying to start with.

```bash
mkdir ${HOME}/.jct/
cp doc/config-sample-file.yaml ${HOME}/.jct/
```

the recorded stacks will be save to the folder specified in the config file.

## built the agent jar that will be used

```bash
mvn clean package
ls -al ./target/
```

## start application with agent

```bash
# (multiline for readability)
java -jar someApplication.jar
  -javaagent:"/path_to/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar"
  -Djct.loglevel=INFO
  -Djct.config=${HOME}/.jct/config-sample-file.yaml (optional, default is under /src/main/resources/META-INF/config.yaml, will be merged)
  -Djct.logDir=/tmp/jct
  -noverify (needed for the moment)
```

## Output Processor(s))

there are multiple ways to handle the captured data. the following processor are currently available:

- **AsyncFileWritingStackProcessor**, writes stacks to dedicated files (see [config.yaml](src/test/resources/integration/test-config-asyncfile.yaml))
- **AsyncUdpStackProcessor**, sends each stack to configured UDP port for further processing (see [config.yaml](src/test/resources/integration/test-config-asyncudp.yaml))

see further down for a fullblown hello world example.

## Message Format

```json
{
    "stack": [
        "de.marcelsauer.helloworld.subA.InSubA.a_1()",
        "de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_1()",
        "de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_2()",
        "de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_2()"
    ],
    "timestampMillis": "1528120883697"
}
```

```
timestampMillis: timestamp milliseconds the first stack entry was started
stack: all captured stack elements during the call, where stack[0] is the entry and stack[n] is the end 
```

## Logging

Files can be found in `jct.logDir`
 
## Debugging

if you need more information about what will be instrumented etc. tune the loglevel. 

```bash
-Djct.loglevel=DEBUG
```

and check the logs ...

# How it works

JCT uses byte code instrumentation. it "magically" weaves tracing information into each instrumented class/method to record calls. it keeps a map of call stacks that it collected and their counts. this is done as long as the instrumented process is running and kept in memory. as soon as the process shuts down, the collected information is gone. 

## Example Hello World walkthrough

In this walkthrough you will start a simple loop that prints "Hello World ..." to the console. the program will be instrumtented with JCT and some data will be collected.

hint: we explicitly excluded the "HelloWorldLoop" in the config otherwise we would never get results as the loop never leaves and JCT never reaches the end of the stack.

now it's time to start it locally ... 

```bash
# produce jar with agent
mvn clean package
# start hello world jar with agent attached to it, (multiline for readability)
java -javaagent:"${PWD}/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar"
	 -Djct.loglevel=INFO 	  
	 -Djct.config=./doc/config-sample-helloworld.yaml
	 -Djct.logDir=/tmp/jct -noverify 
	 -jar "${PWD}/doc/helloworld-loop.jar"
```

you should see "Hello World .." printed to the console multiple times. 

open another tab ... 

check the logs ...

```bash
cat /tmp/jct/jct_agent.log
```

every x seconds the collected stacks will be dumped to a file. you need to aggregate these files yourself however you see fit.

```bash
cat /tmp/stacks/jct_xxxx_xx_xx_xxxxxx_xxx.log

{"timestampMillis" : "1528120452903", "stack" : ["de.marcelsauer.helloworld.subA.InSubA.a_1()","de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_1()","de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_2()","de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_2()"]}
{"timestampMillis" : "1528120452903", "stack" : ["de.marcelsauer.helloworld.subA.InSubA.a_2()"]}
{"timestampMillis" : "1528120452903", "stack" : ["de.marcelsauer.helloworld.subB.InSubB.b_1()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_1()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()"]}
{"timestampMillis" : "1528120452903", "stack" : ["de.marcelsauer.helloworld.subB.InSubB.b_2()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_1()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()"]}
```

you could use some simple aggregation like this

```bash
cd /tmp/stacks/
for file in `find . -name "jct*"`; do cat $file >> all.log; done
cat all.log | perl -pe 's/.*\[(.*)\].*/\1/' | sort | uniq -c

   5 "de.marcelsauer.helloworld.subA.InSubA.a_1()","de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_1()","de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_2()","de.marcelsauer.helloworld.subA.InSubA_InnerClass.a_inner_2()"
   5 "de.marcelsauer.helloworld.subA.InSubA.a_2()"
   5 "de.marcelsauer.helloworld.subB.InSubB.b_1()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_1()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()"
   5 "de.marcelsauer.helloworld.subB.InSubB.b_2()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_1()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()","de.marcelsauer.helloworld.subB.InSubB_InnerClass.b_inner_2()"

```

### JCT -> UDP -> Logstash -> Elasticsearch example

here is a full example to use UDP + logstash + elasticsearch (assumes you have [logstash](https://www.elastic.co/de/downloads/logstash)/[docker](https://www.docker.com/community-edition#/download) on your machine and the UDP processor sends to port 9999)

```bash

# tab1, start logstash with out UDP input and elasticsearch output
logstash -f doc/logstash.conf

#tab2, start elasticsearch
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.2.4

#tab3 (multiline for readability), start helloworld loop that will send captured stacks to UDP->logstash->elastisearch
java -javaagent:"${PWD}/target/java-code-tracer-1.0-SNAPSHOT-jar-with-dependencies.jar" 
	 -Djct.loglevel=INFO 
	 -Djct.config=./doc/config-sample-helloworld-udp.yaml 
	 -Djct.logDir=/tmp/jct 
	 -noverify 
	 -jar "${PWD}/doc/helloworld-loop.jar"

# query captured data
curl -XGET http://127.0.0.1:9200/jct_stacks/_search
```

# Similar Projects/Ideas

- [Datadog dd-trace-java](https://github.com/DataDog/dd-trace-java/)
- [NewRelic](https://newrelic.com/)
- [Call Graph](https://github.com/gousiosg/java-callgraph)

# ToDos, Ideas

- use proper dependency injection instead of static calls

# License

[MIT](LICENSE.txt)
