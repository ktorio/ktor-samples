package io.ktor.samples.metrics

import com.codahale.metrics.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.metrics.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.util.concurrent.*

fun Application.main() {
    install(DefaultHeaders)
    install(Metrics) {
        val reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);
    }
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Ktor: metrics" }
                }
                body {
                    p {
                        +"Hello from Ktor metrics sample application "
                        +"running under ${System.getProperty("java.version")}"
                    }
                }
            }
        }
    }
}