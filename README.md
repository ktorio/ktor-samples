# Samples for Ktor

A collection of ready-to-use samples for [Ktor](https://ktor.io).

* "Hello World" application with various deployment scenarios:
  * [netty](deployment/netty) &mdash; Netty engine application.
  * [netty-embedded](deployment/netty-embedded) &mdash; Netty engine embedded application.
  * [jetty](deployment/jetty) &mdash; Jetty engine application.
  * [jetty-embedded](deployment/jetty-embedded) &mdash; Jetty engine embedded application.
  * [jetty-war](deployment/jetty-war) &mdash; Jetty application server WAR deployment.
  * [tomcat-war](deployment/tomcat-war) &mdash; Tomcat application server WAR deployment.
  * [google-appengine-standard](deployment/google-appengine-standard) &mdash; Google App Engine Standard (war deployment).

* Other build systems and other samples:
  * [maven-netty](other/maven-netty) &mdash; Maven build for Netty engine embedded application. 
  * [maven-google-appengine-standard](other/maven-google-appengine-standard) &mdash; Maven build for Google App Engine Standard (war deployment).
  * [proguard](other/proguard) &mdash; Embedded application minimized with ProGuard.
  * [multiple-connectors](other/multiple-connectors) &mdash; Embedded application listening to several endpoints.
  * [sandbox](other/sandbox) &mdash; Application including all Ktor artifacts, suitable for Scratches files and quick experiments.
  * [simulate-slow-server](other/simulate-slow-server) &mdash; Application showing how to simulate delay in the whole application creating a simple interceptor.
  * [rx](other/rx) &mdash; Application showing how to use RxJava2.
  * [css-dsl](other/css-dsl) &mdash; Application showing how to combine HTML DSL with CSS DSL.
  * [fullstack-mpp](mpp/fullstack-mpp) &mdash; Application showing how to combine Ktor serving Kotlin.JS static scripts.
  * [filelisting](other/filelisting) &mdash; Application showing how to create a file listing for static files. 
  * [structured-logging](other/structured-logging) &mdash; Application showing how to use [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) for structured logging with scoped variables without requiring MDC ThreadStatic.
  * [client-multipart](other/client-multipart) &mdash; Application showing how to do multipart requests with the HttpClient.
  * [client-tools](other/client-tools) &mdash; Application showing several useful extension methods not included in Ktor itself.
  * [sse](other/sse) &mdash; Shows how to use SSE (Server-Sent Events) using Ktor.
  
* Small single-feature samples:
  * [async](feature/async) &mdash; long-running asynchronous computation that happens in a separate thread-pool context.
  * [auth](feature/auth) &mdash; using authorization.
  * [post](feature/post) &mdash; form post and multipart file upload.
  * [sessions](feature/sessions) &mdash; store information that will be kept between requests. 
  * [custom-feature](feature/custom-feature) &mdash; implementation of a custom feature.
  * [html-widget](feature/html-widget) &mdash; custom html widget.
  * Content Negotiation
    * [gson](feature/gson) &mdash; using [Gson](https://github.com/google/gson).
    * [jackson](feature/jackson) &mdash; using [Jackson](https://github.com/FasterXML/jackson).
    * [json-client](feature/json-client) &mdash; HTTP client with JSON support feature. 
  * Dependency Injection
    * [guice](feature/guice) &mdash; using [Guice](https://github.com/google/guice).
    * [kodein](other/di-kodein) &mdash; using [Kodein](https://kodein.org/Kodein-DI/).
  * [locations](feature/locations) &mdash; _experimental_ locations feature.
  * [metrics](feature/metrics) &mdash; metrics feature.
  * [http2-push](feature/http2-push) &mdash; HTTP/2 with server-side push.
  * [ssl](feature/ssl) &mdash; SSL support.
  * [static content](feature/static-content) &mdash; serving static content.
  * [testable](feature/testable) &mdash; application writing tests for Ktor applications.
  * websocket (tba)
 
* Relatively big sample applications:
  * [chat](app/chat) &mdash; simple chat application using websockets and sessions.
  * [httpbin](app/httpbin) &mdash; application implementing (large parts of) HttpBin(1) HTTP Request & Response Service.
  * [kweet](app/kweet) &mdash; messaging application using freemarker templates and experimental locations feature. 
  * [youkube](app/youkube) &mdash; video upload/view application using `kotlinx.html` for rendering and experimental locations feature.
   
## Running samples

Each sample can be run with 

```
./gradlew :<sample-name>:run
```

Then navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
 
Some samples require additional setup as explained in their readme files.
   
## Cut-and-pasting samples

Each sample is a standalone Gradle project that can be cut-and-pasted to get started with your own project. 
Cut-and-paste the directory of the corresponding sample together with 
its build scripts and add [gradle.properties](gradle.properties) from the root
that declares Kotlin, Ktor, and other versions. You may remove reference to the versions that
this particular sample does not use.

Samples with other build systems (Maven) are fully standalone and can be cut-and-pasted to get started.

## Compact directory layout

Samples use compact directory layout whenever possible for ease of navigation:

* `src` directory contains sources directly (no `src/main/kotlin` and package directories).
* `resources` directory contains resources.
* `webapp` directory contains `WEB-INF` directory for samples that are deployed as WARs.
