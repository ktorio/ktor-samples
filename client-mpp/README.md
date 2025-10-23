# Client Multiplatform
A sample project showing how to use a Ktor client in a [multiplatform application](https://ktor.io/docs/getting-started-ktor-client-multiplatform-mobile.html).

## Prerequisites
To build and run the Android application, you need to specify the Android SDK location by either:
* Setting the `ANDROID_HOME` environment variable to your Android SDK path, or
* Creating a `local.properties` file in the project root with `sdk.dir=/path/to/android/sdk`

## Running
An application works on the following platforms: `Android`, `iOS`, `JavaScript`, and `macosArm64`. To run the application, open it in IntelliJ IDEA and do one of the following:
* To run the Android application, use the `client-mpp.androidApp` [run configuration](https://www.jetbrains.com/help/idea/run-debug-configuration.html) created by IntelliJ IDEA automatically.

* To run the iOS application, open the [iosApp](iosApp) directory in Xcode and run it.
* To run the JavaScript application, execute the following command in a project's root directory:
   ```
   ./gradlew :jsApp:jsBrowserDevelopmentRun
   ```
* To run `macosArm64`, execute the following command in a project's root directory:
   ```
   ./gradlew :desktopApp:runDebugExecutableDesktop
   ```