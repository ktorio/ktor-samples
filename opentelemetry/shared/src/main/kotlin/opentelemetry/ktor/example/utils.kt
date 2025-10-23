package opentelemetry.ktor.example

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes

fun getOpenTelemetry(serviceName: String): OpenTelemetry {
    return AutoConfiguredOpenTelemetrySdk.builder().addResourceCustomizer { oldResource, _ ->
        oldResource.toBuilder()
            .putAll(oldResource.attributes)
            .put(ServiceAttributes.SERVICE_NAME, serviceName)
            .build()
    }.build().openTelemetrySdk
}