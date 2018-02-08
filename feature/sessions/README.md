# Session

Sample project for [Ktor](http://ktor.io) demonstrating how to use sessions to keep information in the request
using a cookie or a custom header.

In order to run this sample, you have to execute this command in the repository's root directory:

```bash
./gradlew sessions:run
```

This document has been written for Ktor 0.9.1

## Transfer using Cookies or Custom Headers

You can either use cookies or custom HTTP headers for sessions. The code is roughly the same but you have to
call either the `cookie` method, or `header` method depending on where you want to send the session information.

### Cookies

The Cookie method is intended for browser sessions. It will use a standard
[`Set-Cookie` header](https://developer.mozilla.org/es/docs/Web/HTTP/Headers/Set-Cookie).
Inside the cookie block, you have access to a `cookie` property which allows you to configure the `Set-Cookie` header,
for example, by setting a cookie's `path` or expiration, domain or https related things.

```kotlin
install(Sessions) {
    cookie<SampleSession>("COOKIE_NAME") {
        cookie.path = "/"
        /* ... */
    }
}
```

### Headers

The Header method is intended for APIs both for using in JavaScript XHR requests or by requesting them
from the server side. It is usually easier for API clients to read and generate custom headers than by handling
cookies.

```kotlin
install(Sessions) {
    header<SampleSession>("HTTP_HEADER_NAME") { /* ... */ }
}
```

## Session Content vs Session Id

Ktor allows you to transfer either the session contents or a session id.

### Session Content (client-side)

When using this mode, you can send the actual content of the session to the client as a cookie or a header, as either
raw or transformed.

This mode is considered to be "serverless", since you don't need to store anything on the server side and it is only
the responsibility of the client to store and keep that session. This simplifies the backend, but has security
implications that you need to know to work in this mode.

In this mode you just call `header` or `cookie` methods inside the `install(Sessions)` block with a single argument
with the name of the cookie or the header.

Inside `header` or `cookie` blocks you have the option to call a `transform` method that allows you to transform
the value sent, for example to authenticate or encrypt it.

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

* Serving a session without transform allows people to see the contents of the session clearly and to modify it
* Serving a session with an Authentication transform people can see the contents, but prevents them from modifying it as long
  as you keep your secret hash key safe and use a secure algorithm. It is also possible to use old session strings to go back
  to a previous state.
* Serving a session with an Encrypt transform prevents people from determining the actual contents and modification it,
  but it is still vulnerable to exploitation being returned to previous states.
  
It is possible to store a timestamp or a nonce encryption and authentication, but you will have to limit the
session time or verify it at the server, reducing the benefits of this mode.

So as a rule of thumb you can use this mode only **if it is not a security concern that people could use old
session states**. And if you are using a session to log in the user, **make sure that you are at least authenticating
the session with a transform**, or people will be able to easily access other people's contents.

### Session Id (server-side)

In this mode, you are just sending a Session Id instead of the actual session contents.
This id is used to store its contents on the server side using a specific `SessionStorage`.
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

Alongside SessionStorage there is a `SessionStorageMemory` class that you can use for development.
It is a simple implementation that will keep sessions in in-memory, thus all the sessions are dropped
once you shutdown the server and will grow in memory if they are not dropping old sessions at all.
So it is not intended for production.  

#### directorySessionStorage

As part of the `io.ktor:ktor-server-sessions` artifact, there is a `directorySessionStorage` function
that exposes a session storage that will use a folder for storing sessions on disk.

This function has a first argument of type `File` that is the folder that will store sessions (it will be created
if doesn't exist already).

Also there is a `cache` optional argument that when set, will keep a 60-second in-memory cache to prevent
calling the OS from reading the session from disk at all.

## Baked snippets

### Storing the contents of the session in a cookie

Since no SessionStorage is provided as a `cookie` second argument its contents will be stored in the cookie.

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

## Deciding how to configure sessions

### Cookie vs Header

* Use **Cookies** for plain HTML backends
* Use **Header** for APIs or for XHR requests if it is simpler for your http clients

### Client vs Server

* Use **Server Cookies** if you want to prevent session replays or want to further increase security
  * Use `SessionStorageMemory` for development if you want to drop sessions after stopping server
  * Use `directorySessionStorage` for production environments or to keep sessions after restarting the server
* Use **Client Cookies** if you want a simpler approach without storage on the backend
  * Use it plain if you want to modify it on the fly at the client for testing purposes and don't care about modifications
  * Use it with a transform authenticating and optionally encrypting it to prevent modifications
  * **Do not** use it at all if your session payload is vulnerable to replay attacks
