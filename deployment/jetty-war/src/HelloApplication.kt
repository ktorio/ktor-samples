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
                    title { +"Ktor: jetty-war" }
                }
                body {
                    p {
                        +"Hello from Ktor Jetty WAR deployment sample application "
                        +"running under ${System.getProperty("java.version")}"
                    }
                }
            }
        }
    }
}
