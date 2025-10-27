# Authentication Testing Examples

A comprehensive sample project demonstrating how to write unit tests for various Ktor authentication methods.

## Overview

This project shows how to test different authentication mechanisms supported by Ktor:

- **Basic Authentication** - Username/password authentication using HTTP Basic Auth
- **Digest Authentication** - More secure username/password authentication using HTTP Digest Auth
- **Form Authentication** - HTML form-based authentication
- **Session Authentication** - Cookie-based session authentication
- **Bearer Token Authentication** - Token-based authentication (similar to OAuth/JWT patterns)

## Running Tests

To run all authentication tests:

```shell
./gradlew test
```

To run a specific test class:

```shell
./gradlew test --tests "BasicAuthTest"
./gradlew test --tests "DigestAuthTest"
./gradlew test --tests "FormAuthTest"
./gradlew test --tests "SessionAuthTest"
./gradlew test --tests "BearerTokenAuthTest"
```

## Running the Application

To run the sample application:

```shell
./gradlew run
```

## Test Structure

Each authentication method has:
- A test class demonstrating how to test successful authentication
- A test demonstrating how to test failed authentication
- A test demonstrating how to test missing credentials

## Related Examples

For JWT authentication examples, see the `jwt-auth-tests` directory.
