# Client Native Image

This project is a console application that prints the contents of a web page using a GraalVM native image with a Ktor
Client application.

## Running the application

To run the application, use the following command:

```shell
./gradlew nativeRun
```

## Building the native image

To build the native image, use the following command:

```shell
./gradlew nativeCompile
```

To execute the native image, use the following command:

```shell
./build/native/nativeCompile/ktor-client-native-image
```
