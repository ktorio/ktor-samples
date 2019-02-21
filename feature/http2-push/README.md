# HTTP/2 Push

Sample project for [Ktor](https://ktor.io) demonstrating HTTP/2 protocol server-side push.

This application is written as an embedded application with a custom main function 
[here](src/Main.kt) to generate temporary SSL certificate before starting Ktor Netty engine,
because browsers support HTTP/2 only with SSL.  

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :http2-push:run
```
 
And navigate to [https://localhost:8443/](https://localhost:8443/) to see the sample home page.  
