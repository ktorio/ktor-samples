[![official JetBrains project](https://jb.gg/badges/official-flat-square.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# Samples for Ktor

A collection of ready-to-use samples for [Ktor](https://ktor.io). If you're looking for older samples, please see our
note on [Docs and Samples Migration](https://blog.jetbrains.com/ktor/2020/09/16/docs-and-samples-migration/).

- [Samples for Ktor](#samples-for-ktor)
  - [Applications](#applications)
  - [Server](#server)
  - [Client](#client)
  - [Deployment](#deployment)
  - [Testing](#testing)
  - [License](#license)

## Applications

- [chat](chat/README.md) - A Chat application written using [WebSockets](https://ktor.io/docs/websocket.html) and [Sessions](https://ktor.io/docs/sessions.html).
- [fullstack-mpp](fullstack-mpp/README.md) - An example of using Ktor as a client and server in a Kotlin Multiplatform
  project.
- [httpbin](httpbin/README.md) - HttpBin application implementing (large parts of) [httpbin(1)](https://httpbin.org/) HTTP request & response service.
- [kweet](kweet/README.md) - A messaging application that uses [FreeMarker](https://ktor.io/docs/freemarker.html) templates and the [Locations](https://ktor.io/docs/locations.html) plugin.
- [reverse-proxy](reverse-proxy/README.md) - A simple reverse proxy application.
- [reverse-proxy-ws](reverse-proxy-ws/README.md) - A reverse proxy application written using [WebSockets](https://ktor.io/docs/websocket.html).
- [youkube](youkube/README.md) - A video upload/view application.
- [version-diff](version-diff/README.md) - An application showing the difference between artifacts in two versions of a project.
- [postgres](postgres/README.md) - An application for creating, editing and deleting articles that uses Postgres database running on Docker image as a storage.
- [mongodb](mongodb/README.md) - An application for creating, editing and deleting articles that uses Mongodb running on Docker image as a storage.
- [mvc-web](mvc-web/README.md) - An application for adding and removing wishes to wishlist that uses [FreeMarker](https://ktor.io/docs/freemarker.html) templates and Exposed.
- [opentelemetry](opentelemetry/README.md) - An application that uses Kotlin DSL to work with OpenTelemetry Ktor plugins.

## Server

- [di-kodein](di-kodein/README.md) - An application showing how to use [Kodein](https://kosi-libs.org/kodein)
  with a [Ktor](https://ktor.io) server.
- [filelisting](filelisting/README.md) - An application showing how to [serve static files](https://ktor.io/docs/serving-static-content.html).
- [location-header](location-header/README.md) - An application demonstrating how to use the HTTP `Location`
  headers.
- [sse](sse/README.md) - A Server Sent Events application.
- [structured-logging](structured-logging/README.md) - An application showing how to use [Ktor](https://ktor.io)
  structured logging.
- [openapi](openapi/README.md) - An application showing how to use Ktor's OpenAPI tooling.

## Client

- [client-mpp](client-mpp/README.md) - A sample project showing how to use a Ktor client in
  a [multiplatform application](https://ktor.io/docs/getting-started-ktor-client-multiplatform-mobile.html).
- [client-multipart](client-multipart/README.md) - A sample showing how to send multipart data with the HTTP client.
- [client-tools](client-tools/README.md) - A sample showing several useful extension methods not included in Ktor itself.
- [client-native-image](client-native-image/README.md) - A simple example of how to create a GraalVM native image with a Ktor Client application.

## Deployment

- [graalvm](graalvm/README.md) - Explains how to build and run a Ktor application in the [GraalVM](https://ktor.io/docs/graalvm.html) native image.
- [native-image-server-with-yaml-config](native-image-server-with-yaml-config/README.md) - A sample showing how to build and run a Ktor application in the GraalVM native image with a YAML configuration file.
- [maven-google-appengine-standard](maven-google-appengine-standard/README.md) - A sample showing how to deploy Ktor
  application to Google App Engine using [Maven](https://maven.apache.org/) and [Google App Engine](https://cloud.google.com/appengine/).

## Testing

- [jwt-auth-tests](jwt-auth-tests/README.md) - Shows how to write tests for RSA-signed JWT secured endpoints.

## License

Samples are provided as is under the Apache 2 OSS license.
