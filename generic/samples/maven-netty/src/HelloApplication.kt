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
