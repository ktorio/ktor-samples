package io.ktor.samples.session

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

data class SampleSession(
    val counter: Int
)

/**
 * This will configure a session that stores its content as a cookie, and that signs its content
 * with a secret hash key to prevent modification.
 *
 * We are configuring cookie path to "/" so the cookie is accessible in all routes for the same domain.
 *
 * Note that while the session is signed with a hash and can't be modified without the secret hash key,
 * its contents is **still plain text**.
 */
private fun Application.installCookieSessionSigned() {
    val hashKey = hex("6819b57a326945c1968f45236589")

    install(Sessions) {
        cookie<SampleSession>("SESSION_FEATURE_SESSION") {
            cookie.path = "/" // Specify cookie's path '/' so it can be used in the whole site
            transform(SessionTransportTransformerMessageAuthentication(hashKey, "HmacSHA256"))
        }
    }
}

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)

    installCookieSessionSigned()

    routing {
        get("/") {
            call.respondRedirect("/view")
        }

        get("/view") {
            val session = call.sessions.get<SampleSession>() ?: SampleSession(0)

            call.respondHtml {
                body {
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
