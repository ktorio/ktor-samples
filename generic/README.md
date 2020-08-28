# Samples for Ktor

A collection of ready-to-use samples for [Ktor](https://ktor.io).
 
* [chat](samples/chat): simple chat application using websockets and sessions.
* [httpbin](samples/httpbin): application implementing (large parts of) HttpBin(1) HTTP Request & Response Service.
* [kweet](samples/kweet): messaging application using freemarker templates and experimental locations feature. 
* [youkube](samples/youkube): video upload/view application using `kotlinx.html` for rendering and experimental locations feature.
* [sandbox](samples/sandbox): simple application where you can test out different things with Ktor.
 
and more...  

## Running samples

Each sample can be run with 

```
./gradlew :<sample-name>:run
```

Then navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
 
Some samples require an additional setup as explained in their README files.
   

## Compact directory layout

Samples use compact directory layout whenever possible for ease of navigation:

* `src` directory contains sources directly (no `src/main/kotlin` and package directories).
* `resources` directory contains resources.
* `webapp` directory contains `WEB-INF` directory for samples that are deployed as WARs.
