# Samples for Ktor

A collection of ready-to-use samples for [Ktor](https://ktor.io).
   
## Running samples

Each sample can be run with 

```
./gradlew :<sample-name>:run
```

and navigating to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
 
Some samples may require additional setup steps as explained in their readme files.
   
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
