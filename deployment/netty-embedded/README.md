# Netty embedded

Sample project for [Ktor](https://ktor.io) running as an embedded application with 
[Netty](https://netty.io) engine.

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :netty-embedded:run
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  

## Embedded application 

Embedded Ktor application is started with the following line of code using `Netty` engine:

```kotlin
embeddedServer(Netty, commandLineEnvironment(args)).start()
```

> See [Main.kt](src/Main.kt)
