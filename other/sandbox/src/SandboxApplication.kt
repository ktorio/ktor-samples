package io.ktor.samples.sandbox

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Main entrypoint of the executable that starts a Netty webserver at port 8080
 * and registers the [module].
 *
 * This is a hello-world application, while the important part is that the build.gradle
 * includes all the available artifacts and serves to use as module for a scratch or to autocomplete APIs.
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module()}.start(wait = true)
}

/**
 * Module that just registers the root path / and replies with a text.
 */
fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}