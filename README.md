# Samples for Ktor

A collection of ready-to-use samples for [Ktor](http://ktor.io).

* "Hello World" application with various deployment scenarios:
  * [netty](deployment/netty/README.md) &mdash; Netty engine application.
  * [netty-embedded](deployment/netty-embedded/README.md) &mdash; Netty engine embedded application.
  * [jetty](deployment/jetty/README.md) &mdash; Jetty engine application.
  * [jetty-embedded](deployment/jetty-embedded/README.md) &mdash; Jetty engine embedded application.
  * [jetty-war](deployment/jetty-war/README.md) &mdash; Jetty application server WAR deployment.
  * [tomcat-war](deployment/tomcat-war/README.md) &mdash; Tomcat application server WAR deployment.
  * [google-appengine-standard](deployment/google-appengine-standard/README.md) &mdash; Google App Engine Standard (war deployment).

* Other build systems and other samples:
  * [maven-netty](other/maven-netty/README.md) &mdash; Maven build for Netty engine embedded application. 
  * [maven-google-appengine-standard](other/maven-google-appengine-standard/README.md) &mdash; Maven build for Google App Engine Standard (war deployment).
  * [proguard](other/proguard/README.md) &mdash; Embedded application minimized with ProGuard.
  * [multiple-connectors](other/multiple-connectors/README.md) &mdash; Embedded application listening to several endpoints.
  * [sandbox](other/sandbox/README.md) &mdash; Application including all Ktor artifacts, suitable for Scratches files and quick experiments.
  * [simulate-slow-server](other/simulate-slow-server/README.md) &mdash; Application showing how to simulate delay in the whole application creating a simple interceptor.
  * [rx](other/rx/README.md) &mdash; Application showing how to use RxJava2.
  * [css-dsl](other/css-dsl/README.md) &mdash; Application showing how to combine HTML DSL with CSS DSL.
  * [fullstack-mpp](other/fullstack-mpp/README.md) &mdash; Application showing how to combine Ktor serving Kotlin.JS static scripts.
  * [filelisting](other/filelisting/README.md) &mdash; Application showing how to create a file listing for static files. 
  * [structured-logging](other/structured-logging/README.md) &mdash; Application showing how to use [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) for structured logging with scoped variables without requiring MDC ThreadStatic.
  * [client-multipart](other/client-multipart/README.md) &mdash; Application showing how to do multipart requests with the HttpClient 
  
* Small single-feature samples:
  * [async](feature/async/README.md) &mdash; long-running asynchronous computation that happens in a separate thread-pool context.
  * [auth](feature/auth/README.md) &mdash; using authorization.
  * [post](feature/post/README.md) &mdash; form post and multipart file upload.
  * [sessions](feature/sessions/README.md) &mdash; store information that will be kept between requests. 
  * [custom-feature](feature/custom-feature/README.md) &mdash; implementation of a custom feature.
  * [html-widget](feature/html-widget/README.md) &mdash; custom html widget.
  * Content Negotiation
    * [gson](feature/gson/README.md) &mdash; using [Gson](https://github.com/google/gson).
    * [jackson](feature/jackson/README.md) &mdash; using [Jackson](https://github.com/FasterXML/jackson).
    * [json-client](feature/json-client/README.md) &mdash; HTTP client with JSON support feature. 
  * Dependency Injection
    * [guice](feature/guice/README.md) &mdash; using [Guice](https://github.com/google/guice).
  * [locations](feature/locations/README.md) &mdash; _experimental_ locations feature.
  * [metrics](feature/metrics/README.md) &mdash; metrics feature.
  * [http2-push](feature/http2-push/README.md) &mdash; HTTP/2 with server-side push.
  * [ssl](feature/ssl/README.md) &mdash; SSL support.
  * [static content](feature/static-content/README.md) &mdash; serving static content.
  * [testable](feature/testable/README.md) &mdash; application writing tests for Ktor applications.
  * websocket (tba)
 
* Relatively big sample applications:
  * [chat](app/chat/README.md) &mdash; simple chat application using websockets and sessions.
  * [httpbin](app/httpbin/README.md) &mdash; application implementing (large parts of) HttpBin(1) HTTP Request & Response Service.
  * [kweet](app/kweet/README.md) &mdash; messaging application using freemarker templates and experimental locations feature. 
  * [youkube](app/youkube/README.md) &mdash; video upload/view application using `kotlinx.html` for rendering and experimental locations feature.
   
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