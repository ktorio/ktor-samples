package opentelemetry.ktor.example

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import opentelemetry.ktor.example.plugins.routing.configureRouting

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}
