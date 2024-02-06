package opentelemetry.ktor.example.plugins.opentelemetry.extractions

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.opentelemetry.api.common.AttributesBuilder
import io.opentelemetry.context.Context
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor
import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracingBuilder

// addAttributeExtractor
fun KtorClientTracingBuilder.attributeExtractor(
    extractorBuilder: ExtractorBuilder.() -> Unit = {}
) {
    val builder = ExtractorBuilder().apply(extractorBuilder).build()
    addAttributesExtractors(
        object : AttributesExtractor<HttpRequestData, HttpResponse> {
            override fun onStart(
                attributes: AttributesBuilder,
                parentContext: Context,
                request: HttpRequestData
            ) {
                builder.onStart(OnStartData(attributes, parentContext, request))
            }

            override fun onEnd(
                attributes: AttributesBuilder,
                context: Context,
                request: HttpRequestData,
                response: HttpResponse?,
                error: Throwable?
            ) {
                builder.onEnd(OnEndData(attributes, context, request, response, error))
            }
        }
    )
}

class ExtractorBuilder {
    private var onStart: OnStartData.() -> Unit = {}
    private var onEnd: OnEndData.() -> Unit = {}

    fun onStart(block: OnStartData.() -> Unit) {
        onStart = block
    }

    fun onEnd(block: OnEndData.() -> Unit) {
        onEnd = block
    }

    internal fun build(): Extractor {
        return Extractor(onStart, onEnd)
    }
}

internal class Extractor(val onStart: OnStartData.() -> Unit, val onEnd: OnEndData.() -> Unit)

data class OnStartData(
    val attributes: AttributesBuilder,
    val parentContext: Context,
    val request: HttpRequestData
)

data class OnEndData(
    val attributes: AttributesBuilder,
    val parentContext: Context,
    val request: HttpRequestData,
    val response: HttpResponse?,
    val error: Throwable?
)