package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.ktor.server.request.*
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing

// setSpanKindExtractor
fun KtorServerTracing.Configuration.spanKindExtractor(extract: ApplicationRequest.() -> SpanKind) {
    setSpanKindExtractor {
        SpanKindExtractor<ApplicationRequest> { request: ApplicationRequest ->
            extract(request)
        }
    }
}