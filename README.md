[![official JetBrains project](https://jb.gg/badges/official-flat-square.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# Samples for Ktor

A collection of ready-to-use samples for [Ktor](https://ktor.io). If you're looking for older samples, please see our
note on [Docs and Samples Migration](https://blog.jetbrains.com/ktor/2020/09/16/docs-and-samples-migration/).

## Content

- [Applications](#Applications)
- [Server](#Server)
- [Client](#Client)
- [Deployment](#Deployment)
- [Maven](#Maven)

## Applications

* [chat](chat/README.md) - Chat Application written using WebSockets and Sessions.
* [fullstack-mpp](fullstack-mpp/README.md) - An example of using Ktor as a client and server in a Kotlin Multiplatform
  Project.
* [httpbin](httpbin/README.md) - HttpBin [Ktor](https://ktor.io) application implementing (large parts of)
  [httpbin(1)](https://httpbin.org/) HTTP Request & Response Service.
* [kweet](kweet/README.md) - Messaging application written with [Ktor](https://ktor.io) using freemarker templates and
  [locations](https://ktor.io/docs/locations.html) feature.
* [reverse-proxy](reverse-proxy/README.md) - A simple reverse proxy application written with [Ktor](https://ktor.io).
* [reverse-proxy-ws](reverse-proxy-ws/README.md) - A reverse proxy application using WebSockets written
  with [Ktor](https://ktor.io) and [WebSockets](https://ktor.io/docs/websocket.html).
* [youkube](youkube/README.md) - Video upload/view application written with [Ktor](https://ktor.io).

## Server

* [css-dsl](css-dsl/README.md) - Application showing how to use CSS DSL along HTML CSS with [Ktor](https://ktor.io).
* [di-kodein](di-kodein/README.md) - Application showing how to use [Kodein](https://kodein.org)
  with [Ktor](https://ktor.io) server.
* [filelisting](filelisting/README.md) - Application showing how to use [Ktor](https://ktor.io) to serve static files.
* [location-header](location-header/README.md) - Location Header application demonstrating how to use HTTP Location
  headers.
* [multiple-connectors](multiple-connectors/README.md) - Application showing how to serve both HTTP and HTTPS with a
  single [Ktor](https://ktor.io) server.
* [simulate-slow-server](simulate-slow-server/README.md) - Small application simulating a slow server
  with [Ktor](https://ktor.io) .
* [sse](sse/README.md) - Simple Server Sent Events application written with [Ktor](https://ktor.io).
* [structured-logging](structured-logging/README.md) - Application showing how to use [Ktor](https://ktor.io)
  structured logging.

## Client

* [client-mpp](client-mpp/README.md) - A sample project showing how to use a Ktor client in
  a [multiplatform application](https://ktor.io/docs/http-client-multiplatform.html).
* [client-multipart](client-multipart/README.md) - Sample showing how to send multipart data with HTTP client.
* [client-tools](client-tools/README.md) - Sample showing several useful extension methods not included in Ktor itself.
* [native-client](native-client/README.md) - Sample showing how to use a Ktor HTTP client with kotlin-native.

## Deployment

* [graalvm](graalvm/README.md) - Explains how to build and run Ktor application in
  the [GraalVM](https://www.graalvm.org/) native image.
* [maven-google-appengine-standard](maven-google-appengine-standard/README.md) - Sample showing how to deploy Ktor
  application to Google App Engine using [Maven](https://maven.apache.org/) and
  [Google App Engine](https://cloud.google.com/appengine/).
* [proguard](proguard/README.md) - Sample showing how to pack [Ktor](https://ktor.io) application in minimized Jar using
  [Proguard](https://proguard.io/).

## Maven

* [maven-netty](maven-netty/README.md) - Sample showing how to use [Ktor](https://ktor.io) with
  [Netty](https://netty.io/) and [Maven](https://maven.apache.org/).

## License

Samples are provided as is under the Apache 2 OSS license. 

