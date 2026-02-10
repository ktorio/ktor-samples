package io.ktor.samples.httpbin

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.method
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.anything() {
    route("/anything/{...}") {
        for (method in ALL_METHODS) {
            method(method) {
                handle {
                    val builder = HttpbinResponse.Builder()
                        .argsFromQuery(call.request.queryParameters)
                        .setHeaders(call.request.headers)
                        .setOrigin(call.request.local)
                        .setURL(call.request)
                        .makeUnsafe()
                        .setMethod(call.request.httpMethod)
                        .loadBody(call)

                    call.respond(builder.build())
                }
            }.describe {
                tag("Anything")
                summary = "Anything passed in request"
                responses {
                    HttpStatusCode.OK {
                        description = "Anything passed in request."
                        schema = schemaUnsafe()
                    }
                }
            }
        }
    }
}