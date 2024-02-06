package opentelemetry.ktor.example.plugins.opentelemetry

import opentelemetry.ktor.example.CUSTOM_HEADER
import opentelemetry.ktor.example.CUSTOM_METHOD
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracing
import opentelemetry.ktor.example.plugins.opentelemetry.extractions.*


/**
 * Install OpenTelemetry on the client.
 * You can see usages of new extension functions for [KtorClientTracing].
 */
fun HttpClientConfig<CIOEngineConfig>.installOpenTelemetryOnClient(openTelemetry: OpenTelemetry) {
    install(KtorClientTracing) {
        setOpenTelemetry(openTelemetry)

        emitExperimentalHttpClientMetrics()

        knownMethods(HttpMethod.DefaultMethods + CUSTOM_METHOD)
        capturedRequestHeaders(HttpHeaders.UserAgent)
        capturedResponseHeaders(HttpHeaders.ContentType, CUSTOM_HEADER)

        attributeExtractor {
            onStart {
                attributes.put("start-time", System.currentTimeMillis())
            }
            onEnd {
                attributes.put("end-time", System.currentTimeMillis())
            }
        }
    }
}