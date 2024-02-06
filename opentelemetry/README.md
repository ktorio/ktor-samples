# OpenTelemetry-Ktor Demo

## Running
To run a sample, first, execute the following command in an `opentelemetry` directory:
```bash
./gradlew :runWithDocker
```
It will start a `Jaeger` in the docker container (`Jaeger UI` available on http://localhost:16686/search) and
then it will start a `server` on http://localhost:8080/

Then, to run the client, which will send requests to a server, you can execute the following command in an `opentelemetry` directory:
```bash
./gradlew :client:run
```

[OpenTelemetry](https://opentelemetry.io/) has support for `Ktor`, you can find source code [here](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/ktor).
It contains plugins for client and server: `KtorClientTracing` and `KtorServerTracing`.

## Motivation
This project contains extension functions for plugins that allow you to write code in the Ktor DSL style. \
For example, you can rewrite the next code:
```kotlin
install(KtorServerTracing) {
    ...
    addAttributeExtractor(
        object : AttributesExtractor<ApplicationRequest, ApplicationResponse> {
            override fun onEnd(
                attributes: AttributesBuilder,
                context: Context,
                request: ApplicationRequest,
                response: ApplicationResponse?,
                error: Throwable?
            ) {
                attributes.put("end-time", Instant.now().toEpochMilli())
            }
        }
    )
    ...
}
```
To a more readable for `Ktor` style:
```kotlin
install(KtorServerTracing) {
    ...
    attributeExtractor {
        onEnd {
            attributes.put("end-time", Instant.now().toEpochMilli())
        }
    }    
    ...
}
```
You can find all extensions for the client plugin `KtorClientTracing` in the [extractions](./client/src/main/kotlin/opentelemetry/ktor/example/plugins/opentelemetry/extractions/) folder. \
And you can find all extensions for the server plugin `KtorServerTracing` in the [extractions](./server/src/main/kotlin/opentelemetry/ktor/example/plugins/opentelemetry/extractions/) folder.

## Examples
Let's see what we will see in the `Jaeger UI` after running the server (with Docker) and client:
1. We can see two services that send opentelemetry data: `opentelemetry-ktor-sample-server` and `opentelemetry-ktor-sample-client`:
   ![img.png](images/1.png)
2. If we choose `opentelemetry-ktor-sample-server` service, we will see the next traces:
   ![img.png](images/2.png)
3. And if we choose one of the traces:
   ![img.png](images/3.png)
