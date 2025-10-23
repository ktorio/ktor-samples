# MongoDB sample for Ktor Server

An application for creating, editing and deleting articles that uses MongoDB as a storage.

## Running the Application

Execute this command to run the sample:

```bash
./gradlew run
```

Then, you can open [http://localhost:8080/](http://localhost:8080/) in a browser to create, edit, and delete articles.

By default, the application connects to a MongoDB instance running on `localhost:27017`. You can start one using the provided `docker-compose.yaml`:

```bash
docker-compose up
```

## Running Tests

Tests use [Testcontainers](https://www.testcontainers.org/) to automatically start a MongoDB container, so you don't need to manually set up any infrastructure:

```bash
./gradlew test
```

The MongoDB container will be automatically started before tests run and stopped afterwards.
