package io.ktor.samples.hello

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * For more information about this file: https://ktor.io/servers/configuration.html#hocon-file
 */
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: jetty" }
                }
                body {
                    p {
                        +"Hello from Ktor Jetty engine sample application"
                    }
                }
            }
        }
    }
}
