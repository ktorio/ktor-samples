package opentelemetry.ktor.example.plugins.opentelemetry

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing
import opentelemetry.ktor.example.CUSTOM_HEADER
import opentelemetry.ktor.example.CUSTOM_METHOD
import opentelemetry.ktor.example.getOpenTelemetry
import opentelemetry.ktor.example.plugins.opentelemetry.extractions.*
import java.time.Instant

const val serviceName = "opentelemetry-ktor-sample-server"

/**
 * Install OpenTelemetry on the server.
 * You can see usages of new extension functions for [KtorServerTracing].
 */
fun Application.setupServerTelemetry(): OpenTelemetry {
    val openTelemetry = getOpenTelemetry(serviceName)
    install(KtorServerTracing) {
        setOpenTelemetry(openTelemetry)

        knownMethods(HttpMethod.DefaultMethods + CUSTOM_METHOD)
        capturedRequestHeaders(HttpHeaders.UserAgent)
        capturedResponseHeaders(HttpHeaders.ContentType, CUSTOM_HEADER)

        spanStatusExtractor {
            val success = response?.status()?.let {
                it.isSuccess() || it == HttpStatusCode.SwitchingProtocols
            } ?: false
            if (!success || error != null) {
                spanStatusBuilder.setStatus(StatusCode.ERROR)
            }
        }

        spanKindExtractor {
            if (httpMethod == HttpMethod.Post) {
                SpanKind.PRODUCER
            } else {
                SpanKind.CLIENT
            }
        }

        attributeExtractor {
            onStart {
                attributes.put("start-time", System.currentTimeMillis())
            }
            onEnd {
                attributes.put("end-time", Instant.now().toEpochMilli())
            }
        }
    }

    return openTelemetry
}
