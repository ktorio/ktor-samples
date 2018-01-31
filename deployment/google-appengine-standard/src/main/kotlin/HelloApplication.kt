package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(Routing) {
        get("/") {
            call.respondHtml {
                head {
                    title { +"ktor-appengine-standard" }
                }
                body {
                    p {
                        +"Hello from ktor-appengine-standard application "
                        +"running under ${System.getProperty("java.version")}"
                    }
                }
            }
        }
    }
}
