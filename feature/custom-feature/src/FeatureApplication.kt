package io.ktor.samples.feature

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(CustomHeader) { // Install a custom feature
        headerName = "Hello" // configuration
        headerValue = "World"
    }
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: custom-feature" }
                }
                body {
                    p {
                        +"Hello from Ktor custom feature sample application"
                    }
                }
            }
        }
    }
}
