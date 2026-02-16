package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.RequestConnectionPoint
import io.ktor.http.charset
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.contentCharset
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.uri
import io.ktor.utils.io.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.encoding.Base64

@Serializable
data class ImageErrorResponse(
    val message: String,
    val accept: List<String>
)

@Serializable
data class CookiesResponse(
    val cookies: Map<String, String>
)

@Serializable
data class UuidResponse(
    val uuid: String
)

@Serializable
data class SampleModel(
    val slideshow: SlideShow
) {
    @Serializable
    data class Slide(
        val title: String,
        val type: String,
        val items: List<String>? = null,
    )

    @Serializable
    data class SlideShow(
        val author: String,
        val date: String,
        val slides: List<Slide>,
        val title: String
    )
}

@Serializable
data class GzipResponse(
    val gzipped: Boolean,
    val headers: Map<String, String>,
    val method: String,
    val origin: String
)

@Serializable
data class DeflateResponse(
    val deflated: Boolean,
    val headers: Map<String, String>,
    val method: String,
    val origin: String
)

@Serializable
data class BrotliResponse(
    val brotli: Boolean,
    val headers: Map<String, String>,
    val method: String,
    val origin: String
)

@Serializable
data class UserAuthResponse(
    val authenticated: Boolean,
    val user: String,
)

@Serializable
data class BearerAuthResponse(
    val authenticated: Boolean,
    val token: String,
)

@Serializable
data class UserAgentResponse(
    @SerialName("user-agent")
    val userAgent: String
)


@Serializable
data class IpResponse(
    val origin: String
)

@Serializable
data class HeadersResponse(
    val headers: Map<String, String>
)

@Serializable
data class HttpbinResponse(
    @Serializable(with = SmartValueMapSerializer::class)
    val args: Map<String, List<String>>,
    val data: String? = null,
    val files: Map<String, String>? = null,
    @Serializable(with = SmartValueMapSerializer::class)
    val form: Map<String, List<String>>? = null,
    val headers: Map<String, String>,
    val json: JsonElement? = null,
    val method: String? = null,
    val origin: String,
    val url: String,
    val id: Int? = null,
) {
    class Builder {
        sealed interface RequestBody
        class TextualBody(val payload: String): RequestBody
        class FormUrl(val params: Parameters): RequestBody
        class MultiPartBody(val params: Parameters, val files: Map<String, String>): RequestBody

        private val args = mutableMapOf<String, List<String>>()
        private val headers = mutableMapOf<String, String>()
        private var origin: String = ""
        private var url: String = ""
        private var isUnsafe = false
        private var body: RequestBody? = null
        private var id: Int? = null
        private var method: HttpMethod? = null

        private val ioDispatcher = Dispatchers.IO.limitedParallelism(64)

        suspend fun loadBody(call: ApplicationCall): Builder {
            when (call.request.contentType().withoutParameters()) {
                ContentType.Application.FormUrlEncoded -> {
                    body = FormUrl(call.receiveParameters())
                }
                ContentType.MultiPart.FormData -> {
                    val payload = call.receiveMultipart()
                    val paramsBuilder = ParametersBuilder()
                    val files = mutableMapOf<String, String>()

                    payload.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                part.name?.let { name ->
                                    paramsBuilder[name] = part.value
                                }
                            }
                            is PartData.FileItem -> {
                                part.name?.let { name ->
                                    val contentType = runCatching {
                                        part.headers["Content-Type"]?.let { ContentType.parse(it) }
                                    }.getOrNull()

                                    val charset = if (contentType != null) {
                                        contentType.charset() ?: Charsets.UTF_8
                                    } else {
                                        Charsets.UTF_8
                                    }

                                    files[name] = withContext(ioDispatcher) {
                                        decodeBody(part.provider().toByteArray(), charset)
                                    }
                                    part.dispose()
                                }
                            }
                            else -> {}
                        }
                    }

                    body = MultiPartBody(paramsBuilder.build(), files.toSortedMap())
                }
                else -> {
                    val charset = call.request.contentCharset() ?: Charsets.UTF_8
                    body = TextualBody(
                        withContext(ioDispatcher) { decodeBody(call.receive<ByteArray>(), charset) }
                    )
                }
            }
            return this
        }

        fun makeUnsafe(): Builder {
            isUnsafe = true
            return this
        }

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

        fun setID(id: Int): Builder {
            this.id = id
            return this
        }

        fun setMethod(method: HttpMethod): Builder {
            this.method = method
            return this
        }

        fun build(): HttpbinResponse {
            return HttpbinResponse(
                args = args,
                headers = headers.toSortedMap(),
                origin = origin,
                url = url,
                data = if (isUnsafe) {
                    if (body is TextualBody) {
                        (body as TextualBody).payload
                    } else {
                        ""
                    }
                } else {
                    null
                },
                json = if (isUnsafe) {
                    if (body is TextualBody) {
                        try {
                            Json.decodeFromString<JsonElement>((body as TextualBody).payload)
                        } catch (_: SerializationException) {
                            JsonNull
                        }
                    } else {
                        JsonNull
                    }
                } else {
                    null
                },
                form = if (isUnsafe) {
                    val params = when (body) {
                        is FormUrl -> {
                            (body as FormUrl).params
                        }

                        is MultiPartBody -> {
                            (body as MultiPartBody).params
                        }

                        else -> {
                            Parameters.Empty
                        }
                    }

                    val map = mutableMapOf<String, List<String>>()
                    for ((key, values) in params.entries()) {
                        map[key] = values
                    }
                    map.toSortedMap()
                } else {
                    null
                },
                files = if (isUnsafe) {
                    if (body is MultiPartBody) {
                        (body as MultiPartBody).files
                    } else {
                        mapOf()
                    }
                } else {
                    null
                },
                id = id,
                method = method?.value
            )
        }
    }
}

private fun decodeBody(payload: ByteArray, charset: Charset): String {
    val decoder = charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)

    return try {
        decoder.decode(ByteBuffer.wrap(payload)).toString()
    } catch (_: CharacterCodingException) {
        "data:application/octet-stream;base64,${Base64.encode(payload)}"
    }
}

object SmartValueMapSerializer : KSerializer<Map<String, List<String>>> {
    private val delegate = MapSerializer(String.serializer(), StringOrArraySerializer)
    override val descriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: Map<String, List<String>>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, List<String>> = error("Not Implemented")
}

object StringOrArraySerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("StringOrArray")

    override fun serialize(encoder: Encoder, value: List<String>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("The ${this::class::simpleName} serializer works only with JSON")

        val element = when {
            value.isEmpty() -> JsonPrimitive("")
            value.size == 1 -> JsonPrimitive(value.first())
            else -> JsonArray(value.map { JsonPrimitive(it) })
        }

        jsonEncoder.encodeJsonElement(element)
    }

    override fun deserialize(decoder: Decoder): List<String> = error("Not Implemented")
}