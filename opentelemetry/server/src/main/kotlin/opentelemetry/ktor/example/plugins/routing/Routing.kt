package opentelemetry.ktor.example.plugins.routing

import opentelemetry.ktor.example.CUSTOM_HEADER
import opentelemetry.ktor.example.CUSTOM_METHOD
import opentelemetry.ktor.example.CUSTOM_METHOD_NOT_KNOWN
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.delay
import opentelemetry.ktor.example.plugins.opentelemetry.installOpenTelemetryOnServer
import opentelemetry.ktor.example.plugins.opentelemetry.serviceName

fun Application.configureRouting() {
    install(WebSockets)

    val openTelemetry = installOpenTelemetryOnServer()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/known-methods") {
            method(CUSTOM_METHOD) {
                handle {
                    call.respondText(
                        "For this request you can see `CUSTOM` method instead of default `HTTP` in the Jaeger UI"
                    )
                }
            }

            method(CUSTOM_METHOD_NOT_KNOWN) {
                handle {
                    call.respondText(
                        "For this request you can't see `CUSTOM_NOT_KNOWN` method, " +
                                "you can see default `HTTP` in the Jaeger UI"
                    )
                }
            }
        }

        get("/captured-headers") {
            call.response.headers.append(CUSTOM_HEADER, "it's a custom value")
            call.respondText(
                "You can see tags `http.request.header.user_agent` and `http.response.header.content_type` for " +
                        "all request in the Jaeger UI and also `http.response.header.custom_header` for this request"
            )
        }

        get("/span-status-extractor") {
            call.respond(
                HttpStatusCode.NotFound,
                "For this request you can see tag `error=true` and `Error` icon in the Jaeger UI"
            )
        }

        post("/span-kind-extractor") {
            call.respond("For this request you can see tag `span.kind=producer` in the Jaeger UI")
        }

        get("/attribute-extractor") {
            call.respondText("For this request you can see attribute `start-time` and `end-time` in the Jaeger UI")
        }


        val tracer = openTelemetry.getTracer(serviceName)
        val meter = openTelemetry.getMeter(serviceName)
        route("/opentelemetry") {
            get("/tracer") {
                val span = tracer.spanBuilder("/trace doWork").startSpan()
                try {
                    span.makeCurrent().use { _ ->
                        Span.current().addEvent("Starting the work")
                        call.respondText { "For this request you can see several spans in the Jaeger UI" }
                        Span.current().addEvent("Finished working")
                    }
                } finally {
                    span.end()
                }
            }


            webSocket("/websocket") {
                val span = tracer.spanBuilder("/websocket-server").startSpan()
                val counter = meter.counterBuilder("/websocket-server").build()
                try {
                    repeat(10) {
                        span.makeCurrent().use { _ ->
                            Span.current().addEvent("$it")
                            val frame = incoming.receive() as Frame.Text
                            counter.add(1)
                            outgoing.send(frame)
                        }
                    }
                } finally {
                    delay(100)
                    span.end()
                }
            }
        }
    }
}