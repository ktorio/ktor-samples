# Async

Sample project for [Ktor](https://ktor.io) demonstrating long-running asynchronous
computation that happens in a separate thread-pool context.

For testing, we are specifying a Random and a DelayProvider to the main module.

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :async:run
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  
