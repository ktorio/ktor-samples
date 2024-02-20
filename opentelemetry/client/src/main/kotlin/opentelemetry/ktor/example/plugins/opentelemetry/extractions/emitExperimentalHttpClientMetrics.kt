package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracingBuilder

// setEmitExperimentalHttpClientMetrics
fun KtorClientTracingBuilder.emitExperimentalHttpClientMetrics() {
    setEmitExperimentalHttpClientMetrics(true)
}