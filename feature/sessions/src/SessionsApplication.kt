package io.ktor.samples.sessions

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.hex
import kotlinx.html.*
import java.io.File

data class SampleSession(
    val counter: Int
)

/**
 * This will configure a session that stores its content as a cookie, and that authenticates its content
 * with a secret hash key to prevent modification.
 *
 * We are configuring cookie path to "/" so the cookie is accessible in all routes for the same domain.
 *
 * Note: While the session is signed with a hash and can't be modified without the secret hash key,
 *       its contents is **still plain text**.
 */
private fun Application.installCookieSessionClientSigned() {
    val secretHashKey = hex("6819b57a326945c1968f45236589") // Don't forget to change this value

    install(Sessions) {
        cookie<SampleSession>("SESSION_FEATURE_SESSION") {
            cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
            transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
        }
    }
}

/**
 * This will configure a session that stores its ID in a cookie, while the server
 * keeps the contents of the session without sending it to the client in memory.
 *
 * Note: Sessions with SessionStorageMemory don't have a TTL, sessions keeps growing in memory
 *       so this is intended just for debugging.
 */
private fun Application.installCookieSessionServerMemory() {
    install(Sessions) {
        cookie<SampleSession>("SESSION_FEATURE_SESSION_ID", SessionStorageMemory()) {
            cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
        }
    }
}

/**
 * This will configure a session that stores its ID in a cookie, while the server
 * keeps the contents of the session as a file in the specified directory.
 * If the directory doesn't exists it will be created.
 * cached will allow to keep sessions in-memory 60 seconds to prevent additional reads from the file system.
*/
private fun Application.installCookieSessionServerDirectory() {
    install(Sessions) {
        cookie<SampleSession>(
            "SESSION_FEATURE_SESSION_ID",
            directorySessionStorage(File(".sessions"), cached = true)
        ) {
            cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
        }
    }
}

enum class SessionType {
    CLIENT_SIGNED,
    SERVER_MEMORY,
    SERVER_DIRECTORY
}

fun Application.main() {
    //val sessionType = SessionType.CLIENT_SIGNED
    //val sessionType = SessionType.SERVER_MEMORY
    val sessionType = SessionType.SERVER_DIRECTORY

    install(DefaultHeaders)
    install(CallLogging)

    when (sessionType) {
        SessionType.CLIENT_SIGNED -> installCookieSessionClientSigned()
        SessionType.SERVER_MEMORY -> installCookieSessionServerMemory()
        SessionType.SERVER_DIRECTORY -> installCookieSessionServerDirectory()
    }

    routing {
        get("/") {
            call.respondRedirect("/view")
        }

        get("/view") {
            val session = call.sessions.get<SampleSession>() ?: SampleSession(0)

            call.respondHtml {
                head {
                    title { +"Ktor: sessions" }
                }
                body {
                    p {
                        +"Hello from Ktor Sessions sample application"
                    }
                    p {
                        +"Counter: ${session.counter}"
                    }
                    nav {
                        ul {
                            li { a("/increment") { +"increment" } }
                            li { a("/view") { +"view" } }
                        }
                    }
                }
            }
        }

        get("/increment") {
            val session = call.sessions.get<SampleSession>() ?: SampleSession(0)
            call.sessions.set(session.copy(counter = session.counter + 1))
            call.respondRedirect("/view")
        }
    }
}
