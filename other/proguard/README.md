# Docker

Sample project for [Ktor](http://ktor.io) running as an application with 
inside [Docker](https://www.docker.com/).

## Running

Execute these command in the repository's root directory to run this sample:

```bash
./gradlew :proguard:minimizedJar
java -jar other/proguard/build/libs/my-application.min.jar
```

And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  



