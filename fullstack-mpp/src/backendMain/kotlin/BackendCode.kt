package io.ktor.samples.fullstack.backend

import io.ktor.samples.fullstack.common.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.io.*

fun Application.main() {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")

    routing {
        get("/") {
            call.respondHtml {
                body {
                    +"Hello ${getCommonWorldString()} from Ktor"
                    div {
                        id = "js-response"
                        +"Loading..."
                    }
                    script(src = "/static/output.js") {
                    }
                }
            }
        }
        get("/test") {
            call.respond("I am a test response")
        }
        static("/static") {
            resources()
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080) { main() }.start(wait = true)
}