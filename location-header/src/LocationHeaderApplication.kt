package io.ktor.samples.sandbox

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Main entrypoint of the executable that starts a Netty webserver at port 8080
 * and registers the [module].
 *
 */

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    routing {
        post("/manual") {
            // AF4GH is a sample code for demo purposes
            call.response.header("Location", "/manual/AF4GH")
            call.response.status(HttpStatusCode.Created)
            call.respondText("Manually setting the location header")
        }
        post("/extension") {
            // AF4GH is a sample code for demo purposes
            call.response.created("AF4GH")
            call.respondText("Extension setting the location header")
        }
    }
}

private fun ApplicationResponse.created(id: String) {
    call.response.status(HttpStatusCode.Created)
    call.response.header("Location", "${call.request.uri}/$id")
}
