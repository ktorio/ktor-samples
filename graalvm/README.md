[![official JetBrains project](https://jb.gg/badges/official-flat-square.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# GraalVM sample for Ktor Server

A demo project that shows how to combine Ktor server applications with [GraalVM](https://ktor.io/docs/graalvm.html).

## Steps

1. Make sure that you have [GraalVM](https://graalvm.org) installed and `$GRAALVM_HOME` environment
   variable points to the folder where GraalVM is installed, or alternatively that `native-image` is on your path (if on
   Windows).

2. Run the command `./gradlew nativeCompile` (or `gradlew nativeCompile` on Windows) to build an executable file.

3. The previous step produces an executable file named `graal-server` which can then be run. Open up
   `http://0.0.0.0:8080` to test the server.

4. Run the command `./gradlew nativeTestCompile` (or `gradlew nativeTestCompile` on Windows) to build an executable file for tests.

5. That step produces an executable file named `graal-test-server` which can then be run.

### Current limitations

Using the `Netty` engine is not compatible with GraalVM. Please following
the [corresponding issue](https://youtrack.jetbrains.com/issue/KTOR-2558) for
updates.

## License

This sample is provided as is under the Apache 2 OSS license. 

