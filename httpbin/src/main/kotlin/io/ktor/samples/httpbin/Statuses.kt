package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.method
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.utils.io.ExperimentalKtorApi
import kotlin.random.Random

@OptIn(ExperimentalKtorApi::class)
fun Route.statuses(random: Random) {
    route("/status/{codes}") {
        for (method in ALL_METHODS) {
            method(method) {
                handle {
                    val codes = call.parameters["codes"] ?: return@handle
                    val statusCodes = codes.split(",")
                        .map { it.trim().toIntOrNull() }

                    if (statusCodes.isEmpty() || statusCodes.any { it == null || it < 100 || it > 599 }) {
                        call.respondText(
                            "Invalid status code",
                            status = HttpStatusCode.BadRequest,
                            contentType = ContentType.Text.Html,
                        )
                        return@handle
                    }

                    val code = statusCodes.filterNotNull()[random.nextInt(statusCodes.size)]

                    if (code in setOf(301, 302, 303, 307)) {
                        call.response.headers.append(HttpHeaders.Location, "/redirect/1")
                    }

                    call.respond(HttpStatusCode.fromValue(code))
                }
            }.describe {
                // TODO: Check ordering in UI
                tag("Status codes")
                summary = "Return status code or random status code if more than one are given."
            }
        }
    }
}