package io.ktor.samples.httpbin

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ConnectorType
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.method
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi
import kotlin.text.toIntOrNull

@OptIn(ExperimentalKtorApi::class)
fun Route.redirects(engine: ApplicationEngine) {
    get("/absolute-redirect/{n}") {
        val n = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

        if (n == 0) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val connectors = engine.resolvedConnectors()
        require(connectors.isNotEmpty())
        with(connectors.first()) {
            val proto = if (type == ConnectorType.HTTPS) "https" else "http"
            val portPart = when (port) {
                80 if type == ConnectorType.HTTP -> ""
                443 if type == ConnectorType.HTTPS -> ""
                else -> ":$port"
            }

            val baseUrl = "$proto://$host$portPart"
            call.respondRedirect(baseUrl + if (n <= 1) "/get" else "/absolute-redirect/${n-1}")
        }
    }.describe {
        tag("Redirects")
        summary = "Absolutely 302 Redirects n times."
        responses {
            HttpStatusCode.Found {
                description = "A redirection."
            }
        }
    }

    route("/redirect-to") {
        for (method in ALL_METHODS) {
            method(method) {
                handle {
                    val url = call.queryParameters["url"]

                    if (url == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@handle
                    }

                    var statusCode = call.request.queryParameters["status_code"]?.toIntOrNull() ?: 302
                    if (statusCode !in 300..399) {
                        statusCode = 302
                    }

                    call.response.headers.append(HttpHeaders.Location, url)
                    call.respond(HttpStatusCode.fromValue(statusCode))
                }
            }.describe {
                tag("Redirects")
                summary = "302/3XX Redirects to the given URL."
                parameters {
                    query("url") {
                        required = true
                    }
                    query("status_code") {
                        schema = jsonSchema<Int>()
                    }
                }
                responses {
                    HttpStatusCode.Found {
                        description = "A redirection."
                    }
                }
            }
        }
    }

    get("/redirect/{n}") {
        val n = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

        if (n == 0) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        call.respondRedirect(if (n <= 1) "/get" else "/relative-redirect/${n-1}")
    }.describe {
        tag("Redirects")
        summary = "302 Redirects n times."
        responses {
            HttpStatusCode.Found {
                description = "A redirection."
            }
        }
    }

    get("/relative-redirect/{n}") {
        val n = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

        if (n == 0) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        call.respondRedirect(if (n <= 1) "/get" else "/relative-redirect/${n-1}")
    }.describe {
        tag("Redirects")
        summary = "Relatively 302 Redirects n times."
        responses {
            HttpStatusCode.Found {
                description = "A redirection."
            }
        }
    }
}