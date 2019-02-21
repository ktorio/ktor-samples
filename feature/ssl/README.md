# SSL

Sample project for [Ktor](https://ktor.io) with SSL connection.

This application is written as an embedded application with a custom main function 
[here](src/Main.kt) to generate temporary SSL certificate before starting Ktor Netty engine.  

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :ssl:run
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.
You should get redirected to secure page at [https://localhost:8443/](https://localhost:8443/).  
Security dialog is shown because this sample uses self-signed temporary certificate.
  
## HTTPS Redirect

This sample also installs HTTPS redirect feature to automatically redirect to HTTPS port:

```kotlin
install(HttpsRedirect) {
    sslPort = 8443
}
```

