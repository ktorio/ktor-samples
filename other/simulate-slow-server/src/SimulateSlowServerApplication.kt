package io.ktor.samples.simulateslowserver

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.experimental.time.*
import java.time.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    intercept(ApplicationCallPipeline.Infrastructure) {
        delay(Duration.ofSeconds(1L))
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
