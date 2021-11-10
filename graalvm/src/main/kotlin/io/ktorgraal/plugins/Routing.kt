package io.ktorgraal.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Starting point for a Ktor app:
    routing {
        get("/") {
            call.respondText("Hello GraalVM!")
            call.application.environment.log.info("Call made to /")
        }
    }

}
