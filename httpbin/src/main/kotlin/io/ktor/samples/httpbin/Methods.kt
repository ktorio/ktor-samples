package io.ktor.samples.httpbin

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.method
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.methods() {
    get("/get") {
        val builder = HttpbinResponse.Builder()
            .argsFromQuery(call.request.queryParameters)
            .setHeaders(call.request.headers)
            .setOrigin(call.request.local)
            .setURL(call.request)

        call.respond(builder.build())
    }.describe {
        tag("HTTP Methods")
        summary = "The request's query parameters."
        responses {
            HttpStatusCode.OK {
                description = "The request’s query parameters."
                schema = schemaGet()
            }
        }
    }

    for ((method, path) in listOf(
        HttpMethod.Post to "/post",
        HttpMethod.Put to "/put",
        HttpMethod.Patch to "/patch",
        HttpMethod.Delete to "/delete",
    )) {
        route(path) {
            method(method) {
                handle {
                    val builder = HttpbinResponse.Builder()
                        .argsFromQuery(call.request.queryParameters)
                        .setHeaders(call.request.headers)
                        .setOrigin(call.request.local)
                        .setURL(call.request)
                        .makeUnsafe()
                        .loadBody(call)

                    call.respond(builder.build())
                }
            }.describe {
                tag("HTTP Methods")
                summary = "The request's ${method.value} parameters."
                responses {
                    HttpStatusCode.OK {
                        description = "The request's ${method.value} parameters."
                        schema = partialSchema<HttpbinResponse>(
                            "HttpbinUnsafe",
                            "args", "data", "files", "form", "headers", "json", "origin", "url"
                        )
                    }
                }
            }
        }
    }
}