package io.ktor.samples.hello

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: maven-netty" }
                }
                body {
                    p {
                        +"Hello from Ktor Maven Netty engine sample application"
                    }
                }
            }
        }
    }
}
