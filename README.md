# Samples for Ktor

A collection of ready-to-use samples for [Ktor](http://ktor.io).

* "Hello World" application with various deployment scenarios:
  * [netty](deployment/netty/README.md) &mdash; Netty engine embedded application.
  * [jetty](deployment/jetty/README.md) &mdash; Jetty engine embedded application.
  * jetty-war &mdash; Jetty (war deployment).
  * tomcat-war &mdash; Tomcat (war deployment).
  * [google-appengine-standard](deployment/google-appengine-standard/README.md) &mdash; Google App Engine Standard (war deployment).

* "Hello World" application with other build systems:
  * [maven-netty](other/maven-netty/README.md) &mdash; Maven build for Netty engine embedded application. 
  * [maven-google-appengine-standard](other/maven-google-appengine-standard/README.md) &mdash; Maven build for Google App Engine Standard (war deployment).  
  
* Small single-feature samples:
  * [async](feature/async/README.md) &mdash; long-running asynchronous computation that happens in a separate thread-pool context.
  * [post](feature/post/README.md) &mdash; form post and multipart file upload.
  * [sessions](feature/sessions/README.md) &mdash; store information that will be kept between requests. 
  * [custom-feature](feature/custom-feature/README.md) &mdash; implementation of a custom feature.
  * auth
  * content negotiation
    * gson
    * jackson
  * dependency injection
    * guice
  * locations
  * metrics
  * http2 push
  * static content
  * testable application
  * websocket (tba)
 
* Big sample applications:
  * [chat](app/chat/README.md) &mdash; Simple chat application using websockets and sessions.
  * httpbin
  * kweet
  * youkube
   
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
