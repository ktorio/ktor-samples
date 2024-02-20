package opentelemetry.ktor.example

import io.ktor.http.*

const val SERVER_HOST = "0.0.0.0"
const val SERVER_PORT = 8080

// Custom HTTP methods
val CUSTOM_METHOD = HttpMethod("CUSTOM")
val CUSTOM_METHOD_NOT_KNOWN = HttpMethod("CUSTOM_NOT_KNOWN")

// Custom HTTP headers
const val CUSTOM_HEADER = "Custom-Header"
