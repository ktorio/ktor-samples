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

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        val currentDir = File(".").absoluteFile
        environment.log.info("Current directory: $currentDir")

        val webDir = listOf(
            "web",
            "../src/frontendMain/web",
            "src/frontendMain/web",
            "mpp/fullstack-mpp/src/frontendMain/web"
        ).map {
            File(currentDir, it)
        }.firstOrNull { it.isDirectory }?.absoluteFile ?: error("Can't find 'web' folder for this sample")

        environment.log.info("Web directory: $webDir")

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
                            +"require(['/static/fullstack-mpp.js'], function(frontend) { frontend.io.ktor.samples.fullstack.frontend.helloWorld('Hi'); });\n"
                        }
                    }
                }
            }
            static("/static") {
                files(webDir)
            }
        }
    }.start(wait = true)
}