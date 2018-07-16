package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(CallLogging)
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: google-appengine-standard" }
                }
                body {
                    p {
                        +"Hello from Ktor Google Appengine Standard sample application"
                    }
                }
            }
        }
    }
}
