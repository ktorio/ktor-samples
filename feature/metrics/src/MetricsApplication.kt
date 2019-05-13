package io.ktor.samples.metrics

import com.codahale.metrics.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.metrics.dropwizard.*
import io.ktor.routing.*
import kotlinx.html.*
import java.util.concurrent.*

fun Application.main() {
    install(DefaultHeaders)
    install(DropwizardMetrics) {
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
                        +"Hello from Ktor metrics sample application"
                    }
                }
            }
        }
    }
}