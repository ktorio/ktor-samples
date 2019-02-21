# Multiple Connectors

Sample project for [Ktor](https://ktor.io) with an embedded server with multiple connector endpoints. 

## Running

Execute these command in the repository's root directory to run this sample:

```bash
./gradlew :multiple-connectors:run
```

And navigate to [http://localhost:8080/](http://localhost:8080/) (or from another computer in the same network) to see one public page.
And navigate to [http://localhost:9090/](http://localhost:9090/) to see a different private page just accessible to localhost.
