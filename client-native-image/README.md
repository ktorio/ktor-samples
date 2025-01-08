
# Client Native Image

This project is a simple example of how to create a GraalVM native image with a Ktor Client application.

## Running the application

To run the application, use the following command:

```shell
./gradlew nativeRun
```

## Building the native image

To build the native image, you can use the following command:

```shell
./gradlew nativeCompile
```

To run execute the native image, you can use the following command:

```shell
./build/native/nativeCompile/ktor-client-native-image
```
