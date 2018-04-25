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
                    title { +"Ktor: Docker" }
                }
                body {
                    val runtime = Runtime.getRuntime()
                    p { +"Hello from Ktor Netty engine running in Docker sample application" }
                    p { +"Runtime.getRuntime().availableProcessors(): ${runtime.availableProcessors()}" }
                    p { +"Runtime.getRuntime().freeMemory(): ${runtime.freeMemory()}" }
                    p { +"Runtime.getRuntime().totalMemory(): ${runtime.totalMemory()}" }
                    p { +"Runtime.getRuntime().maxMemory(): ${runtime.maxMemory()}" }
                }
            }
        }
    }
}
