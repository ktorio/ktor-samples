package io.ktor.samples.hello

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.main() {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: maven-google-appengine-standard" }
                }
                body {
                    p {
                        +"Hello from Ktor Maven Google Appengine Standard sample application"
                    }
                }
            }
        }
    }
}
