package io.ktor.samples.openapi

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureOpenApi()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
