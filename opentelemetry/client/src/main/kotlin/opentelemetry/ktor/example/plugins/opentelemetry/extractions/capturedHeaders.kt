package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracingBuilder

// setCapturedRequestHeaders
fun KtorClientTracingBuilder.capturedRequestHeaders(vararg headers: String) {
    capturedRequestHeaders(headers.asIterable())
}

fun KtorClientTracingBuilder.capturedRequestHeaders(headers: Iterable<String>) {
    setCapturedRequestHeaders(headers.toList())
}

// setCapturedResponseHeaders
fun KtorClientTracingBuilder.capturedResponseHeaders(vararg headers: String) {
    capturedResponseHeaders(headers.asIterable())
}

fun KtorClientTracingBuilder.capturedResponseHeaders(headers: Iterable<String>) {
    setCapturedResponseHeaders(headers.toList())
}