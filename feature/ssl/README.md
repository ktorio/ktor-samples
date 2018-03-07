# SSL

Sample project for [Ktor](http://ktor.io) with SSL connection.

This application is written as an embedded application with a custom main function 
[here](src/Main.kt) to generate temporary SSL certificate before starting Ktor Netty engine.  

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :ssl:run
```
 
And navigate to [https://localhost:8443/](https://localhost:8443/) to see the sample home page via SSL. 
Security dialog is shown because this sample uses self-signed temporary certificate.
  
