package io.ktor.samples.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.ldap.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Defines a typed location to be used along the [Locations] feature.
 */
@Location("/files") class Files()

/**
 * Defines a Ktor module that shows how to configure and use LDAP authentication.
 */
fun Application.basicAuthWithLdap() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    routing {
        // Defines a route for /files matching any HTTP Method.
        location<Files> {
            authentication {
                basic("files") {
                    validate {  credentials ->
                        ldapAuthenticate(credentials, "ldap://localhost:389", "cn=%s ou=users") {
                            if (it.name == it.password) {
                                UserIdPrincipal(it.name)
                            } else null
                        }
                    }
                }
            }

            handle {
                call.response.status(HttpStatusCode.OK)
                call.respondText("""
                Directory listing

                .
                ..
                dir1
                and so on
                """)
            }
        }
    }
}
