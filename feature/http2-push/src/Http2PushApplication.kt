package io.ktor.samples.http2push

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.push("/style.css")
            call.respondHtml {
                head {
                    title { +"Ktor: http2-push" }
                    styleLink("/style.css")
                }
                body {
                    p {
                        +"Hello from Ktor HTTP/2 push sample application"
                    }
                }
            }
        }
        get("/style.css") {
            call.respondText("p { color: blue }", contentType = ContentType.Text.CSS)
        }
    }
}
