# Native image server using YAML configuration file

This example shows how to build a native image for Ktor application using YAML confinguration.

## Build

To build the application in native image, you need to execute the following command:

```bash
./gradlew nativeCompile
```

## Run

To run the application, you need to execute the following command:

```bash
./build/native/nativeCompile/com.example.ktor-sample
```

## Update metadata

If new dependencies are added to the project or a project has been modified, some classes may not be included in
the native image. To fix this, you need to update the metadata file.

First you need to run the application with the agent to generate the metadata file:

```bash
./gradlew -Pagent run
```

While the application is running, you need to access basic application functions

It is important to gracefully shutdown application to generate the metadata file. For this project you can call shutdown
url: [http://localhost:8080/shutdown](http://localhost:8080/shutdown)

Finally, you can copy to the metadata files:
```bash
./gradlew metadataCopy --task run --dir src/main/resources/META-INF/native-image
```
