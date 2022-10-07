# Full-stack multiplatform application

A full-stack sample project for [Ktor](https://ktor.io) running as an embedded application and serving
a static folder with kotlin-js code sharing code with the backend.

The structure:
* [src/commonMain](src/commonMain) - common code shared between the backend and the frontend
* [src/backendMain](src/backendMain) - Ktor backend including fullstack-common code
* [src/frontendMain](src/frontendMain) - kotlin-js code including fullstack-common code

## Running

Run this project with:

```
./gradlew run
```

Then, navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.
