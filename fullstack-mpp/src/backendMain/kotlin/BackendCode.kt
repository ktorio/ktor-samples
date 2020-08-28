package io.ktor.samples.fullstack.backend

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.content.*
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
                    script(src = "/static/ktor-samples-fullstack-mpp-frontend.js") {
                    }
                }
            }
        }
        static("/static") {
            resource("ktor-samples-fullstack-mpp-frontend.js")
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { main() }.start(wait = true)
}