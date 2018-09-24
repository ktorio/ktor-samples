package io.ktor.samples.testable

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * The `Application.main` part is Kotlin idiomatic that specifies that the main method is
 * an extension of the [Application] class, and thus can be accessed like a normal member `myapplication.main()`.
 */
fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.respondText("Hello from Ktor Testable sample application")
        }
    }
}
