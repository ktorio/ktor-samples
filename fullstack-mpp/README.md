# Fullstack MultiPlatform Project application

Fullstack sample project for [Ktor](https://ktor.io) running as an embedded application and serving
a static folder with kotlin-js code sharing code with the backend.

The structure:
* `src/commonMain` - common code shared between the backend and the frontend
* `src/backendMain` - ktor backend including fullstack-common code
* `src/frontendMain` - kotlin-js code including fullstack-common code

## Running

Run this project with:

```
./gradlew run
```

And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.
