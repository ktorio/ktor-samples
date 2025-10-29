# Authentication Testing Guide

This document provides guidance on testing authentication in Ktor applications, with examples from the `auth-tests` and `jwt-auth-tests` samples.

## Overview

Ktor supports multiple authentication mechanisms. Testing these mechanisms is crucial to ensure your API security is working correctly. This guide covers the following authentication types:

1. **Basic Authentication** - Simple username/password authentication using HTTP Basic Auth
2. **Digest Authentication** - More secure username/password authentication using HTTP Digest Auth
3. **Form Authentication** - HTML form-based authentication
4. **Session Authentication** - Cookie-based session authentication
5. **Bearer Token Authentication** - Token-based authentication (OAuth/JWT style)
6. **JWT Authentication** - JSON Web Token authentication with RSA signatures

## Sample Projects

### auth-tests

The [auth-tests](auth-tests/README.md) sample provides comprehensive examples of testing Basic, Digest, Form, Session, and Bearer Token authentication. It includes:

- **22 test cases** covering various scenarios
- Tests for successful authentication
- Tests for failed authentication (wrong credentials, missing credentials)
- Tests for multi-user scenarios
- Practical examples using Ktor's `testApplication` DSL

### jwt-auth-tests

The [jwt-auth-tests](jwt-auth-tests/README.md) sample demonstrates testing JWT authentication with RSA signatures. It shows:

- How to mock a JWK provider for testing
- How to generate test tokens
- How to verify JWT-protected endpoints

## Testing Patterns

### Using testApplication

All authentication tests use Ktor's `testApplication` DSL:

```kotlin
@Test
fun testBasicAuthSuccess() = testApplication {
    application {
        main()  // Install your application module
    }
    
    val response = client.get("/protected") {
        basicAuth("username", "password")
    }
    
    assertEquals(HttpStatusCode.OK, response.status)
}
```

### Testing Different Auth Types

#### Basic Authentication

```kotlin
client.get("/basic") {
    basicAuth("user1", "password1")
}
```

#### Bearer Token

```kotlin
client.get("/bearer") {
    bearerAuth(token)
}
```

#### Form Authentication

```kotlin
client.post("/login-form") {
    contentType(ContentType.Application.FormUrlEncoded)
    setBody(
        listOf(
            "username" to "user1",
            "password" to "password1"
        ).formUrlEncode()
    )
}
```

#### Session Authentication

Sessions require cookie handling:

```kotlin
val client = createClient {
    install(HttpCookies)
}

// Login to establish session
client.post("/login-form") {
    contentType(ContentType.Application.FormUrlEncoded)
    setBody(...)
}

// Use session in subsequent requests
client.get("/session")
```

## Best Practices

1. **Test Both Success and Failure Cases** - Always test successful authentication and various failure scenarios
2. **Test Missing Credentials** - Ensure your endpoints properly reject requests without credentials
3. **Test Invalid Credentials** - Verify that wrong passwords/tokens are rejected
4. **Test Authorization** - Beyond authentication, test that users can only access resources they're authorized for
5. **Use Test Fixtures** - Create helper functions for common setup (test users, tokens, etc.)
6. **Mock External Services** - For JWT/OAuth, mock the token providers or key providers
7. **Test Multi-User Scenarios** - Ensure different users get the right authentication and authorization

## Common Testing Scenarios

### Scenario 1: Protected Endpoint

Test that a protected endpoint:
- Returns 401 when no credentials are provided
- Returns 401 when invalid credentials are provided
- Returns 200 with valid credentials
- Returns the correct user-specific data

### Scenario 2: Login Flow

Test the login flow:
- Successful login with valid credentials
- Failed login with invalid credentials
- Session/token is properly created on successful login
- Session/token can be used to access protected resources

### Scenario 3: Logout Flow

Test the logout flow:
- Session/token is invalidated after logout
- Previously accessible resources return 401 after logout

## Running the Tests

To run all authentication tests:

```bash
# Basic, Digest, Form, Session, Bearer Token tests
cd auth-tests
./gradlew test

# JWT tests
cd jwt-auth-tests
./gradlew test
```

## Additional Resources

- [Ktor Authentication Documentation](https://ktor.io/docs/authentication.html)
- [Ktor Testing Documentation](https://ktor.io/docs/testing.html)
- [HTTP Authentication Standards](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
