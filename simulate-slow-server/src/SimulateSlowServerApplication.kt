package io.ktor.samples.simulateslowserver

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.time.*
import java.time.*

/**
 * Main entrypoint for this application.
 *
 * This starts a webserver using Netty at port 8080.
 * It configures the [module].
 */
fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

/**
 * This module [Application.intercept]s the infrastructure pipeline adding a step where
 * it asynchronously suspends the execution for a second. Effectively delaying every single request.
 */
fun Application.module() {
    intercept(ApplicationCallPipeline.Plugins) {
        delay(Duration.ofSeconds(1L))
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
