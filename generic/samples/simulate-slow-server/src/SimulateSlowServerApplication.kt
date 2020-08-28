package io.ktor.samples.simulateslowserver

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.time.*
import java.time.*

/**
 * Main entrypoint for this application.
 *
 * This starts a webserver using Netty at port 8080.
 * It configures the [module].
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

/**
 * This module [Application.intercept]s the infrastructure pipeline adding a step where
 * it asynchronously suspends the execution for a second. Effectively delaying every single request.
 */
fun Application.module() {
    intercept(ApplicationCallPipeline.Features) {
        delay(Duration.ofSeconds(1L))
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
