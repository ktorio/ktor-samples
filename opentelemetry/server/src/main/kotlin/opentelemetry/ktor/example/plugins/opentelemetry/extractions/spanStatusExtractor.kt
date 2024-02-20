package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusBuilder
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing

// setStatusExtractor
fun KtorServerTracing.Configuration.spanStatusExtractor(extract: SpanStatusData.() -> Unit) {
    setStatusExtractor {
        SpanStatusExtractor<ApplicationRequest, ApplicationResponse> { spanStatusBuilder: SpanStatusBuilder,
                                                                       request: ApplicationRequest,
                                                                       response: ApplicationResponse?,
                                                                       throwable: Throwable? ->
            extract(SpanStatusData(spanStatusBuilder, request, response, throwable))
        }
    }
}

data class SpanStatusData(
    val spanStatusBuilder: SpanStatusBuilder,
    val request: ApplicationRequest,
    val response: ApplicationResponse?,
    val error: Throwable?
)