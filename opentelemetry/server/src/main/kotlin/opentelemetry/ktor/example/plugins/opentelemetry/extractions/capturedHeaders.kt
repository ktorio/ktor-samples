package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing

// setCapturedRequestHeaders
fun KtorServerTracing.Configuration.capturedRequestHeaders(vararg headers: String) {
    capturedRequestHeaders(headers.asIterable())
}

fun KtorServerTracing.Configuration.capturedRequestHeaders(headers: Iterable<String>) {
    setCapturedRequestHeaders(headers.toList())
}

// setCapturedResponseHeaders
fun KtorServerTracing.Configuration.capturedResponseHeaders(vararg headers: String) {
    capturedResponseHeaders(headers.asIterable())
}

fun KtorServerTracing.Configuration.capturedResponseHeaders(headers: Iterable<String>) {
    setCapturedResponseHeaders(headers.toList())
}