package io.ktor.samples.httpbin

import io.ktor.http.*
import io.ktor.http.Headers
import io.ktor.openapi.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi

fun main() {
    embeddedServer(Netty, System.getenv("PORT")?.toIntOrNull() ?: 8080) {
        module()
    }.start(wait = true)
}

@OptIn(ExperimentalSerializationApi::class)
val prettyJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

val ALL_METHODS = listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Delete)

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class, ExperimentalKtorApi::class)
fun Application.module(random: Random = Random.Default) {
    install(CORS) {
        anyHost()
        allowCredentials = true
    }
    install(DefaultHeaders)
    install(AutoHeadResponse)
    install(ContentNegotiation) {
        json(prettyJson)
    }
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { status ->
            call.respondText(status = status, contentType = ContentType.Text.Html) {
                """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>404 Not Found</title>
                    </head>
                    <body>
                        <h1>Not Found</h1>
                        <p>The requested URL was not found on the server. 
                        If you entered the URL manually please check your spelling and try again.</p>
                    </body>
                    </html>
                """.trimIndent()
            }
        }

        status(HttpStatusCode.InternalServerError) { status ->
            call.respondText(status = status, contentType = ContentType.Text.Html) {
                """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>500 Internal Server Error</title>
                    </head>
                    <body>
                        <h1>Internal Server Error</h1>
                        <p>The server encountered an internal error and was unable to complete your request. 
                        Either the server is overloaded or there is an error in the application.</p>
                    </body>
                    </html>
                """.trimIndent()
            }
        }

        status(HttpStatusCode.ServiceUnavailable) { status ->
            call.respondText(status = status, contentType = ContentType.Text.Html) {
                """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>503 Service Unavailable</title>
                    </head>
                    <body>
                        <h1>Service Unavailable</h1>
                        <p>The server is currently unable to handle the request due to temporary overload or maintenance.
                        Please try again later.</p>
                    </body>
                    </html>
                """.trimIndent()
            }
        }
    }

    install(Authentication) {
        basic("basic") {
            realm = "Fake Realm"
            validate { credentials ->
                val user = parameters["user"] ?: return@validate null
                val password = parameters["password"] ?: return@validate null

                if (user == credentials.name && password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            charset = null
        }
        basic("hidden-basic") {
            challenge = {
                respond(HttpStatusCode.NotFound)
            }
            realm = "Fake Realm"
            validate { credentials ->
                val user = parameters["user"] ?: return@validate null
                val password = parameters["password"] ?: return@validate null

                if (user == credentials.name && password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            charset = null
        }
        bearer("bearer") {
            authenticate { credentials ->
                credentials.token
            }
        }
        digest("digest") {
            realm = "Fake Realm"
            getAlgorithm = {
                parameters["algorithm"] ?: "MD5"
            }

            digestProvider { _, realm ->
                val user = parameters["user"] ?: return@digestProvider null
                val password = parameters["password"] ?: return@digestProvider null
                val algorithm = parameters["algorithm"] ?: "MD5"

                MessageDigest.getInstance(algorithm).digest(
                    "$user:$realm:$password".toByteArray()
                )
            }
            validate { credentials ->
                if (credentials.userName.isNotEmpty()) {
                    UserIdPrincipal(credentials.userName)
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/") {
            call.respondRedirect("/swagger", permanent = true)
        }

        swaggerUI(path = "/swagger") {
            info = OpenApiInfo(
                "BounceBin",
                "1.0",
                "A simple HTTP Request & Response Service (httpbin.org clone developed with Ktor)."
            )
            components = Components(
                securitySchemes = mapOf(
                    "digest" to ReferenceOr.Value(
                        HttpSecurityScheme("digest", description = "HTTP Digest Authentication")
                    )
                )
            )
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants().filter { it.path != "/" }
            }
        }

        methods()
        auth()
        statuses(random)
        requestInspection()
        responseInspection()
        responseFormats()
        dynamic()
        cookies()
        images()
        redirects(engine)
        anything()

        get("/forms/post") {
            call.respondResource("form.html")
        }.describe {
            tag("Other")

            summary = "HTML form that posts to /post"
            responses {
                HttpStatusCode.OK {
                    ContentType.Text.Html()
                }
            }
        }
    }
}

fun Headers.toSortedMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for ((key, values) in entries()) {
        map[key] = values.joinToString(separator = ",")
    }
    return map.toSortedMap()
}