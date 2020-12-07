# ProGuard

Sample project for [Ktor](https://ktor.io) packed as a JAR and minimized using [ProGuard](https://www.guardsquare.com/en/proguard). 

## Running

In order to minimize a JAR you need to use JDK 8 because file `lib/rt.jar` 
that is required by [ProGuard](https://www.guardsquare.com/en/proguard) is [removed](https://docs.oracle.com/javase/9/migrate/toc.htm#JSMIG-GUID-A78CC891-701D-4549-AA4E-B8DD90228B4B) in JDK 9.   
Execute these command in the repository's root directory to run this sample:

```bash
./gradlew :proguard:minimizedJar
java -jar other/proguard/build/libs/my-application.min.jar
```

And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
