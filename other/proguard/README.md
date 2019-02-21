# ProGuard

Sample project for [Ktor](https://ktor.io) packed as a JAR and minimized using [ProGuard](https://www.guardsquare.com/en/proguard). 

## Running

Execute these command in the repository's root directory to run this sample:

```bash
./gradlew :proguard:minimizedJar
java -jar other/proguard/build/libs/my-application.min.jar
```

And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
