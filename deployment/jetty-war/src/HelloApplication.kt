package io.ktor.samples.hello

import io.ktor.application.*
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
    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: jetty-war" }
                }
                body {
                    p {
                        +"Hello from Ktor Jetty WAR deployment sample application"
                    }
                }
            }
        }
    }
}
