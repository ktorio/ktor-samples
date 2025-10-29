package io.ktor.samples.auth

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Serializable
data class User(val username: String, val password: String)

@Serializable
data class UserSession(val username: String)

@Serializable
data class TokenResponse(val token: String)

// Simulated user database
val users = mapOf(
    "user1" to "password1",
    "user2" to "password2",
    "testuser" to "testpass"
)

fun Application.main() {
    install(ContentNegotiation) {
        json()
    }

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        // Basic Authentication
        basic("auth-basic") {
            realm = "Access to the '/basic' path"
            validate { credentials ->
                if (users[credentials.name] == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        // Digest Authentication
        digest("auth-digest") {
            realm = "Access to the '/digest' path"
            digestProvider { userName, realm ->
                // In production, retrieve the digest from a secure storage
                // For testing, we compute it from the password
                users[userName]?.let { password ->
                    // The digest is MD5(username:realm:password)
                    getDigestFunction("MD5") { "ktor-digest-auth" }("$userName:$realm:$password")
                }
            }
        }

        // Form Authentication
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (users[credentials.name] == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        // Session Authentication
        session<UserSession>("auth-session") {
            validate { session ->
                if (users.containsKey(session.username)) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
            }
        }

        // Bearer Token Authentication
        bearer("auth-bearer") {
            realm = "Access to bearer protected routes"
            authenticate { tokenCredential ->
                // In production, validate token against a database or JWT verification
                // For testing, we use a simple validation
                val token = tokenCredential.token
                // Simple token format: "token_<username>"
                if (token.startsWith("token_")) {
                    val username = token.removePrefix("token_")
                    if (users.containsKey(username)) {
                        UserIdPrincipal(username)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Authentication Testing Examples - See README for details")
        }

        // Basic Authentication endpoint
        authenticate("auth-basic") {
            get("/basic") {
                val principal = call.principal<UserIdPrincipal>()
                call.respondText("Hello, ${principal?.name}! (authenticated with Basic Auth)")
            }
        }

        // Digest Authentication endpoint
        authenticate("auth-digest") {
            get("/digest") {
                val principal = call.principal<UserIdPrincipal>()
                call.respondText("Hello, ${principal?.name}! (authenticated with Digest Auth)")
            }
        }

        // Form Authentication endpoints
        authenticate("auth-form") {
            post("/login-form") {
                val principal = call.principal<UserIdPrincipal>()
                if (principal != null) {
                    call.sessions.set(UserSession(principal.name))
                    call.respondText("Login successful for ${principal.name}")
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Login failed")
                }
            }
        }

        // Session Authentication endpoint
        authenticate("auth-session") {
            get("/session") {
                val session = call.principal<UserSession>()
                call.respondText("Hello, ${session?.username}! (authenticated with Session)")
            }
        }

        post("/logout") {
            call.sessions.clear<UserSession>()
            call.respondText("Logged out successfully")
        }

        // Bearer Token endpoints
        post("/login-bearer") {
            val user = call.receive<User>()
            if (users[user.username] == user.password) {
                val token = "token_${user.username}"
                call.respond(TokenResponse(token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        authenticate("auth-bearer") {
            get("/bearer") {
                val principal = call.principal<UserIdPrincipal>()
                call.respondText("Hello, ${principal?.name}! (authenticated with Bearer Token)")
            }
        }
    }
}
