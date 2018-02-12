# Sessions

Sample project for [Ktor](http://ktor.io) demonstrating how to use sessions to keep information in the request
using a cookie or a custom header.

In order to run this sample, you have to execute this command in the repository's root directory:

```bash
./gradlew sessions:run
```

## What are they for?

Sessions are a mechanism to provide a temporal or persistent state across stateless HTTP requests.

Different use-cases include authentication and authorization, user tracking, keeping information at client
like a shopping cart, and more.

## Basic usage

Sessions are usually represented as immutable data classes:

```kotlin
data class SampleSession(val name: String, val value: Int)
```

Configuration. Sessions require to specify a [cookie/header](#cookies-headers) name,
optional [server-side storage](#client-server) and a class associated to the session.
You can read more about [deciding how to configure sessions](#configuring):

```kotlin
install(Sessions) {
    cookie<SampleSession>("COOKIE_NAME")
}
```

When handling requests, you can get, set or clear your sessions:

```kotlin
val session = call.sessions.get<SampleSession>() // Gets a session of this type or null if not available
call.sessions.set(SampleSession(name = "John", value = 12)) // Sets a session of this type
call.sessions.clear<SampleSession>() // Clears the session of this type 
```

<a id="configuring"></a>
## Deciding how to configure sessions

### Cookie vs Header

* Use [**Cookies**](#cookies) for plain HTML backends.
* Use [**Header**](#headers) for APIs or for XHR requests if it is simpler for your http clients.

### Client vs Server

* Use [**Server Cookies**](#server-cookies) if you want to prevent session replays or want to further increase security
  * Use `SessionStorageMemory` for development if you want to drop sessions after stopping server
  * Use `directorySessionStorage` for production environments or to keep sessions after restarting the server
* Use [**Client Cookies**](#client-cookies) if you want a simpler approach without storage on the backend
  * Use it plain if you want to modify it on the fly at the client for testing purposes and don't care about modifications
  * Use it with a transform authenticating and optionally encrypting it to prevent modifications
  * **Do not** use it at all if your session payload is vulnerable to replay attacks. [Security examples here](#security).

## Multiple sessions

You might want to have several sessions for a single application, with different configurations in the same application.
For example:

* Storing user preferences, or cart information as a client-side cookie.
* While storing the user login in the server.

```kotlin
install(Sessions) {
    cookie<SessionCart>("SESSION_CART_LIST") {
        cookie.path = "/shop" // Just accessible in '/shop/*' subroutes
    }
    cookie<SessionLogin>(
        "SESSION_LOGIN",
        directorySessionStorage(File(".sessions"), cached = true)
    ) {
        cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
    }
}
```

<a id="cookies-headers"></a>
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

<a id="client-server"></a>
## Session Content vs Session Id

Ktor allows you to transfer either the session contents or a session id.

<a id="client-cookies"></a>
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

Also have in mind that if your secure key is compromised, a person with the key will be able to generate any
session payload potentially impersonating anyone.

Also it is important to notice that changing the key will invalidate all the sessions from all the users.

<a id="server-cookies"></a>
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
It is a simple implementation that will keep sessions in-memory, thus all the sessions are dropped
once you shutdown the server and will constantly grow in memory since the implementation do not discard
old sessions at all.

This implementation is not intended for production environments.

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

You have to include an additional artifact for the `directorySessionStorage` function.

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

<a id="security"></a>
## Security examples for client-side sessions

If you plan to use client-side sessions, you need to understand the security implications it has. You have to keep
your secret hash/encryption keys safe, and if they are compromised the person with the keys would potentially
be able to impersonate any user. And changing the key will invalidate all the sessions previously generated.

### Good usages for client-side cookies:

**Storing user preferences, such as language, cookie acceptation and things like that.**

No security concerns for this. Just preferences. If anyone could ever modify the session. No harm is done at all.

**Shopping cart information**

If this information acts as a *wish list*, would just be like preferences. No possible harm here. 

**Storing user login using a immutable user id or an email address.**

Should be ok if at least authenticated (and with the knowledge of general risks) since in normal circumstances
people won't be able to change it to impersonate another person. And if you store a unique immutable session id,
using old session payloads, would just give access to already own users. 

### Bad usages for client-side cookies:

**Using session as cache. For example storing user's redeemable points.**

If you are using session as cache to prevent reading from a database for example *user points* that an user can
use to purchase things. It is exploitable, since the user could purchase an item, but not to update the session
or using an old session payload that would have more points.

**Using session to store a mutable user name.**

Consider you are storing the user name in the session to keep login information. But also allows to change
the username of an actual user. A malicious user could create an account, and rename its user several times
storing valid session payloads for each user name. So if a new user is createed using a previously renamed
user name, the malicious user would have access to that account.
Server-side sessions are also vulnerable to this, but the attacker would have to keep those sessions alive.
