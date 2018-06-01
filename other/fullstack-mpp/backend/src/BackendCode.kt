package io.ktor.samples.fullstack.backend

import io.ktor.application.*
import io.ktor.content.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.samples.fullstack.common.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import java.io.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        environment.log.info("Current directory: ${File(".").absoluteFile}")

        routing {
            get("/") {
                call.respondHtml {
                    body {
                        +"Hello ${getCommonWorldString()} from Ktor"
                        div {
                            id = "js-response"
                            +"Loading..."
                        }
                        script(src = "/static/require.min.js") {
                        }
                        script {
                            +"require.config({baseUrl: '/static'});\n"
                            +"require(['/static/fullstack-mpp-frontend.js'], function(frontend) { frontend.io.ktor.samples.fullstack.frontend.helloWorld('Hi'); });\n"
                        }
                    }
                }
            }
            static("/static") {
                files(File("../frontend/web"))
                files(File("other/fullstack-mpp/frontend/web"))
            }
        }
    }.start(wait = true)
}