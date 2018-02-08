# Session

Sample project for [Ktor](http://ktor.io) demonstrating how to use sessions to keep user information between request
using a cookie.

You can either store the session contents in a cookie, optionally authenticated (or with other transformations)
Or to put a session identifier in the cookie, with the actual session content stored at the backend side.

You can have to install `Sessions` feature, and then call `cookie` for storing sessions in a cookie.

Cookie expects the name of the cookie and the type of a class that will be stored and optionally a `SessionStorage`.
If no SessionStorage is provided, the actual contents of the session will be stored in the cookie. And if you provide
a SessionStorage, then the cookie will contain just a session id that will be used to resolve the cookie in the backend.

Inside `cookie` block you have access to a cookie property where you can adjust cookie properties like "path".
Don't forge to set the `cookie.path` to `"/"` in the case you want that session to be accessible in all your routes
for the same domain.

Inside `cookie` block also, you can apply transforms to transform the contents of the cookie, to for example authenticate
it with a hmac hash to prevent modifying its contents.

## Storing the contents of the session in a cookie

Since no SessionStorage is provided as `cookie` second argument its contents will be stored in the cookie.

```kotlin
install(Sessions) {
    cookie<SampleSession>("SESSION_FEATURE_SESSION") {
        cookie.path = "/"
        transform(SessionTransportTransformerMessageAuthentication(hex("6819b57a326945c1968f45236589"), "HmacSHA256"))
    }
}
```

## Storing a session id in a cookie, and storing session contents in-memory

`SessionStorageMemory` don't have parameters at this point.

```kotlin
install(Sessions) {
    cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory()) {
        cookie.path = "/"
    }
}
```

## Storing a session id in a cookie, and storing session contents in a file

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
