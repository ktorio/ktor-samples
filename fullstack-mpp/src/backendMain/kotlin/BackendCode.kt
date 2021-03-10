package io.ktor.samples.fullstack.backend

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.samples.fullstack.common.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { main() }.start(wait = true)
}