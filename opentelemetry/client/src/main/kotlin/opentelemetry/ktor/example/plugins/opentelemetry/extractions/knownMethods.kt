package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.ktor.http.*
import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracingBuilder

// setKnownMethods
fun KtorClientTracingBuilder.knownMethods(vararg methods: HttpMethod) {
    knownMethods(methods.asIterable())
}

fun KtorClientTracingBuilder.knownMethods(methods: Iterable<HttpMethod>) {
    setKnownMethods(methods.map { it.value }.toSet())
}