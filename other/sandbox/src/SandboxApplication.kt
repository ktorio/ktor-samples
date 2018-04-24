package io.ktor.samples.sandbox

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module()}
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}