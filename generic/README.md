# Samples for Ktor

A collection of ready-to-use samples for [Ktor](https://ktor.io).
 
* [chat](samples/chat) &mdash; simple chat application using websockets and sessions.
* [httpbin](samples/httpbin) &mdash; application implementing (large parts of) HttpBin(1) HTTP Request & Response Service.
* [kweet](samples/kweet) &mdash; messaging application using freemarker templates and experimental locations feature. 
* [youkube](sample/youkube) &mdash; video upload/view application using `kotlinx.html` for rendering and experimental locations feature.
   
## Running samples

Each sample can be run with 

```
./gradlew :<sample-name>:run
```

Then navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
 
Some samples require additional setup as explained in their readme files.
   

## Compact directory layout

Samples use compact directory layout whenever possible for ease of navigation:

* `src` directory contains sources directly (no `src/main/kotlin` and package directories).
* `resources` directory contains resources.
* `webapp` directory contains `WEB-INF` directory for samples that are deployed as WARs.
