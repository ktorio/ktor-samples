# Maven Netty

Sample project for [Ktor](https://ktor.io) running as an embedded application with 
[Netty](https://netty.io) engine with [Maven](https://maven.apache.org) build script. 

## Running

Run this project with:

```
./mvnw compile exec:java
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  

## Packaging

Package a single fat JAR archive with:

```
./mvnw package
```
