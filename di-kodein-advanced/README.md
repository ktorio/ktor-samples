# Kodein-DI (Advanced)

A sample project for [Ktor](https://ktor.io) demonstrating how to integrate
[Kodein DI](https://kodein.org/Kodein-DI/) for dependency injection.

This example shows:

- How to bind dependencies using Kodein
- How to auto-discover controllers from the DI container
- How to register routes dynamically
- How to use type-safe routing with `@Resource`

---

## Running the Sample

Run the application using:

```bash
./gradlew run
```

Once started, open your browser and navigate to:

```text
http://localhost:8080/users
```

You should see a list of sample users (`test`, `demo`).

Clicking a user will navigate to:

```text
http://localhost:8080/users/{name}
```

---

## Project Structure Overview

- `Users.Controller`  
  Handles user-related routes.

- `Users.Repository`  
  Provides an in-memory implementation of the user repository.

- `kodeinApplication`  
  Bootstraps DI and automatically registers all discovered controllers.

---

This sample focuses specifically on demonstrating dependency injection
with controller discovery using Kodein in a Ktor application.