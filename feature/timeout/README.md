# Timeout

Sample project for [Ktor](https://ktor.io) demonstrating how to use timeout.

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :timeout:run
```

This example consists of two endpoints. First endpoint `/timeout` emulates some long-running process that might hangup.
Second endpoint `/proxy` represents a proxy to `/timeout` that protects a user against such hang-ups. If user connects
to `/proxy` and request hanged proxy will automatically abort request using `HttpTimeout` feature.