package io.ktor.samples.staticcontent

import io.ktor.application.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*
import java.io.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: static-content" }
                    styleLink("static/css/styles.css")
                    script(src = "static/js/script.js") {}
                }
                body {
                    p {
                        +"Hello from Ktor static content sample application "
                        +"running under ${System.getProperty("java.version")}"
                    }
                    p {
                        +"Current directory is ${System.getProperty("user.dir")}"
                    }
                    img(src = "static/image.png")
                }
            }
        }
        static("static") {
            // When running under IDEA make sure that working directory is set to this sample's project folder
            staticRootFolder = File("files")
            files("css")
            files("js")
            file("image.png")
            file("random.txt", "image.png")
            default("index.html")
        }
        static("custom") {
            staticRootFolder = File("/tmp") // Establishes a root folder
            files("public") // For this to work, make sure you have /tmp/public on your system
            static("themes") {
                // services /custom/themes
                files("data")
            }
        }
    }
}
