package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: netty-embedded" }
                }
                body {
                    p {
                        +"Hello from Ktor Netty embedded engine sample application "
                        +"running under ${System.getProperty("java.version")}"
                    }
                }
            }
        }
    }
}
