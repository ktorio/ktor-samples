package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
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
