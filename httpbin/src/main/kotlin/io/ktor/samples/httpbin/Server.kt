package io.ktor.samples.httpbin

import io.ktor.http.Headers
import io.ktor.http.Parameters
import io.ktor.http.RequestConnectionPoint
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.component1
import kotlin.collections.component2


@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
    install(AutoHeadResponse)
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }
        )
    }

    routing {
        get("/get") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)

            call.respond(builder.build())
        }
        post("/post") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)

            call.respond(builder.build())
        }
        put("/put") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)

            call.respond(builder.build())
        }
        patch("/patch") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)

            call.respond(builder.build())
        }
        delete("/delete") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)

            call.respond(builder.build())
        }
    }
}

@Serializable
data class HttpbinResponse(
    @Serializable(with = SmartValueMapSerializer::class)
    val args: Map<String, List<String>>,
    val headers: Map<String, String>,
    val origin: String,
    val url: String,
) {
    class Builder {
        private val args = mutableMapOf<String, List<String>>()
        private val headers = mutableMapOf<String, String>()
        private var origin: String = ""
        private var url: String = ""

        fun argsFromQuery(params: Parameters): Builder {
            for ((key, values) in params.entries()) {
                args[key] = values
            }
            return this
        }

        fun setHeaders(requestHeaders: Headers): Builder {
            for ((key, values) in requestHeaders.entries()) {
                headers[key] = values.joinToString(separator = ",")
            }
            return this
        }

        fun setOrigin(point: RequestConnectionPoint): Builder {
            origin = point.remoteAddress
            return this
        }

        fun setURL(request: ApplicationRequest): Builder {
            url = "${request.local.scheme}://${request.local.serverHost}"

            when (val port = request.local.serverPort) {
                80 if request.local.scheme == "http" -> {}
                443 if request.local.scheme == "https" -> {}
                0 -> {} // Test engine
                else -> {
                    url += ":$port"
                }
            }

            url += request.uri
            return this
        }

        fun build(): HttpbinResponse {
            return HttpbinResponse(args, headers.toSortedMap(), origin, url)
        }
    }
}

object SmartValueMapSerializer : KSerializer<Map<String, List<String>>> {
    private val delegate = MapSerializer(String.serializer(), StringOrArraySerializer)
    override val descriptor = delegate.descriptor
    override fun serialize(
        encoder: kotlinx.serialization.encoding.Encoder,
        value: Map<String, List<String>>
    ) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, List<String>> = TODO("Not Required")
}

object StringOrArraySerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("StringOrArray")

    override fun serialize(
        encoder: kotlinx.serialization.encoding.Encoder,
        value: List<String>
    ) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("The ${this::class::simpleName} serializer works only with JSON")

        val element = when {
            value.isEmpty() -> JsonPrimitive("")
            value.size == 1 -> JsonPrimitive(value.first())
            else -> JsonArray(value.map { JsonPrimitive(it) })
        }

        jsonEncoder.encodeJsonElement(element)
    }

    override fun deserialize(decoder: Decoder): List<String> = TODO("Not required")
}