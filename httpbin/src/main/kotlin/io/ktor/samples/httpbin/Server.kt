package io.ktor.samples.httpbin

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import kotlin.io.encoding.Base64


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
                .setOrigin(call.request.local)
                .setURL(call.request)
                .makeUnsafe()
                .loadBody(call)

            call.respond(builder.build())
        }
        put("/put") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)
                .makeUnsafe()

            call.respond(builder.build())
        }
        patch("/patch") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)
                .makeUnsafe()

            call.respond(builder.build())
        }
        delete("/delete") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)
                .makeUnsafe()

            call.respond(builder.build())
        }
    }
}

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
    val origin: String,
    val url: String,
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
                }
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