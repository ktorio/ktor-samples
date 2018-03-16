package io.ktor.samples.ssl

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(HttpsRedirect) {
        sslPort = 8443
    }
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: SSL" }
                }
                body {
                    p {
                        +"Hello from Ktor SSL sample application"
                    }
                }
            }
        }
    }
}
