# Fullstack MultiPlatform Project application

Fullstack sample project for [Ktor](http://ktor.io) running as an embedded application and serving
a static folder with kotlin-js code sharing code with the backend.

The structure:
* `fullstack-mpp-common` - common code shared between the backend and the frontend
* `fullstack-mpp-backend` - ktor backend including fullstack-common code
* `fullstack-mpp-frontend` - kotlin-js code including fullstack-common code 

## Running

Run this project with:

```
./gradlew :fullstack-mpp-backend:run
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
