# Session

Sample project for [Ktor](http://ktor.io) demonstrating how to use sessions to keep information between request
using a cookie or a custom header.

In order to run this sample, you have to execute this command in the repository's root directory:

```bash
./gradlew sessions:run
```

This document has been written for Ktor 0.9.1

## Transfer using Cookies or Custom Headers

You can either use cookies or custom HTTP headers for sessions. The code would be roughly the same but you have to
call `cookie` method, or `header` method depending where do you want to send session information.

### Cookies

Cookie method is intended for browser sessions. It will use a standard
[`Set-Cookie` header](https://developer.mozilla.org/es/docs/Web/HTTP/Headers/Set-Cookie).
Inside cookie block, you have access to a `cookie` property that allows you to configure the `Set-Cookie` header
to for example setting cookie's `path` or expiration, domain or https related things.

```kotlin
install(Sessions) {
    cookie<SampleSession>("COOKIE_NAME") {
        cookie.path = "/"
        /* ... */
    }
}
```

### Headers

Header method is intended for APIs both for consuming in JavaScript XHR requests or by requesting them
from server side. It is usually easier for API clients to read and generate custom headers than by handling
cookies.

```kotlin
install(Sessions) {
    header<SampleSession>("HTTP_HEADER_NAME") { /* ... */ }
}
```

## Session Content vs Session Id

Ktor allows to transfer either the session contents or a session id.

### Session Content (client-side)

When using this mode, you send the actual content of the session to the client as a cookie or a header either as
raw or transformed.

This mode is considered to be "serverless", since you don't need to store anything at server side and it is just
the responsibility of the client to store and keep that session. This simplifies the backend, but has security
implications that you need to know to work in this mode.

In this mode you just call `header` or `cookie` methods inside `install(Sessions)` block with a single argument
with the name of the cookie or the header.

Inside `header` or `cookie` blocks you can optionally call a `transform` method that allows you to transform
the value sent, to for example authenticate or encrypt it.

```kotlin
install(Sessions) {
    val secretHashKey = hex("6819b57a326945c1968f45236589")

    cookie<SampleSession>("SESSION_FEATURE_SESSION") {
        cookie.path = "/"
        transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
    }
}
```

**Notes about security:**

* Serving session without transforms allows people to see in the clear the contents of the session and to modify it
* Serving session with an Authentication transform allows people to see the contents, but prevents modifying it as long
  as you keep your secret hash key safe and use a secure algorithm. Also allows to use old session strings to go back
  to a previous state.
* Serving session with an Encrypt transform prevents from actual determining the contents and preventing modification,
  but still is vulnerable to going back to previous states.
  
It is possible to store a timestamp or a nonce encrypted and authenticated, but you would have to time limit the
session or to verify it at the server, reducing the benefits from this mode.

So as a rule of thumb you can use this mode just **if it is not a security concern that people could use old
session states**. And if you are using session to login the user, **make sure that you are at least authenticating
the session with a transform**, or people will be able to easily access to other people contents.

### Session Id (server-side)

In this mode, you are just sending a Session Id instead of the actual session contents.
This id is used to store its contents at server side using a specific `SessionStorage`.
This mode is used when you specify a second argument with a storage in the `cookie` or `header` methods.

Example:

```kotlin
install(Sessions) {
    cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory()) {
        cookie.path = "/"
    }
}
```

#### SessionStorageMemory

Along SessionStorage there is a `SessionStorageMemory` class that you can use for development.
It is a simple implementation that keep sessions in in-memory, thus all the sessions are dropped
once you shutdown the server and also will grow in memory without dropping old sessions at all.
So it is not intended for production.  

#### directorySessionStorage

As part of the `io.ktor:ktor-server-sessions` artifact, there is a `directorySessionStorage` function
that exposes a session storage that will use a folder for storing sessions in disk.

This function have a first argument of type `File` that is the folder that will store sessions (it will be created
if doesn't exists already).

Also has a `cache` optional argument that when set, will keep a 60-seconds in-memory cache to prevent
calling the OS for reading the session from disk at all.

## Baked snippets

### Storing the contents of the session in a cookie

Since no SessionStorage is provided as `cookie` second argument its contents will be stored in the cookie.

```kotlin
install(Sessions) {
    val secretHashKey = hex("6819b57a326945c1968f45236589")
    
    cookie<SampleSession>("SESSION_FEATURE_SESSION") {
        cookie.path = "/"
        transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
    }
}
```

### Storing a session id in a cookie, and storing session contents in-memory

`SessionStorageMemory` don't have parameters at this point.

```kotlin
install(Sessions) {
    cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory()) {
        cookie.path = "/"
    }
}
```

### Storing a session id in a cookie, and storing session contents in a file

You have to include an additional artefact for the `directorySessionStorage` function.

`compile "io.ktor:ktor-server-sessions:$ktor_version" // Required for directorySessionStorage`

```kotlin
install(Sessions) {
    cookie<SampleSession>(
        "SESSION_FEATURE_SESSION_ID",
        directorySessionStorage(File(".sessions"), cached = true)
    ) {
        cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
    }
}
```
