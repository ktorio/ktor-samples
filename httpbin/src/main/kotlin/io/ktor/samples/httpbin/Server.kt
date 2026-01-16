package io.ktor.samples.httpbin

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
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
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.random.Random

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

private val headerRegex = """[A-Za-z0-9-_]+""".toRegex()

@OptIn(ExperimentalSerializationApi::class)
fun Application.module(random: Random = Random.Default) {
    install(DefaultHeaders)
    install(AutoHeadResponse)
    install(ContentNegotiation) {
        json(json)
    }
    install(Authentication) {
        basic("basic") {
            realm = "Fake Realm"
            validate { credentials ->
                val user = parameters["user"] ?: return@validate null
                val password = parameters["password"] ?: return@validate null

                if (user == credentials.name && password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            charset = null
        }
        basic("hidden-basic") {
            challenge = {
                respond(HttpStatusCode.NotFound)
            }
            realm = "Fake Realm"
            validate { credentials ->
                val user = parameters["user"] ?: return@validate null
                val password = parameters["password"] ?: return@validate null

                if (user == credentials.name && password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            charset = null
        }
        bearer("bearer") {
            authenticate { credentials ->
                credentials.token
            }
        }
        digest("digest") {
            realm = "Fake Realm"
            getAlgorithm = {
                parameters["algorithm"] ?: "MD5"
            }

            digestProvider { _, realm ->
                val user = parameters["user"] ?: return@digestProvider null
                val password = parameters["password"] ?: return@digestProvider null
                val algorithm = parameters["algorithm"] ?: "MD5"

                MessageDigest.getInstance(algorithm).digest(
                    "$user:$realm:$password".toByteArray()
                )
            }
            validate { credentials ->
                if (credentials.userName.isNotEmpty()) {
                    UserIdPrincipal(credentials.userName)
                } else {
                    null
                }
            }
        }
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
                .loadBody(call)

            call.respond(builder.build())
        }
        patch("/patch") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)
                .makeUnsafe()
                .loadBody(call)

            call.respond(builder.build())
        }
        delete("/delete") {
            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)
                .makeUnsafe()
                .loadBody(call)

            call.respond(builder.build())
        }

        authenticate("basic") {
            get("/basic-auth/{user}/{password}") {
                val principal = call.principal<UserIdPrincipal>()

                call.respond(UserAuthResponse(
                    authenticated = principal != null,
                    user = principal?.name ?: "",
                ))
            }
        }

        authenticate("hidden-basic") {
            get("/hidden-basic-auth/{user}/{password}") {
                val principal = call.principal<UserIdPrincipal>()

                call.respond(UserAuthResponse(
                    authenticated = principal != null,
                    user = principal?.name ?: "",
                ))
            }
        }

        authenticate("bearer") {
            get("/bearer") {
                val token = call.principal<String>()

                call.respond(BearerAuthResponse(
                    authenticated = token != null,
                    token = token ?: "",
                ))
            }
        }

        authenticate("digest") {
            get("/digest-auth/{user}/{password}/{algorithm?}") {
                val principal = call.principal<UserIdPrincipal>()

                call.respond(UserAuthResponse(
                    authenticated = principal != null,
                    user = principal?.name ?: "",
                ))
            }
        }

        route("/status/{codes}") {
            handle {
                val codes = call.parameters["codes"] ?: return@handle
                val statusCodes = codes.split(",")
                    .map { it.trim().toIntOrNull() }

                if (statusCodes.isEmpty() || statusCodes.any { it == null || it < 100 || it > 599 }) {
                    call.respondText(
                        "Invalid status code",
                        status = HttpStatusCode.BadRequest,
                        contentType = ContentType.Text.Html,
                    )
                    return@handle
                }

                val code = statusCodes.filterNotNull()[random.nextInt(statusCodes.size)]

                if (code in setOf(301, 302, 303, 307)) {
                    call.response.headers.append(HttpHeaders.Location, "/redirect/1")
                }

                call.respond(HttpStatusCode.fromValue(code))
            }
        }

        get("/headers") {
            val headers = mutableMapOf<String, String>()
            for ((key, values) in call.request.headers.entries()) {
                headers[key] = values.joinToString(separator = ",")
            }
            call.respond(HeadersResponse(headers.toSortedMap()))
        }

        get("/ip") {
            call.respond(IpResponse(call.request.local.remoteAddress))
        }

        get("/user-agent") {
            call.respond(UserAgentResponse(call.request.userAgent() ?: ""))
        }

        get("/cache") {
            if (call.request.headers[HttpHeaders.IfModifiedSince] != null
                || call.request.headers[HttpHeaders.IfNoneMatch] != null) {

                call.respond(HttpStatusCode.NotModified)
                return@get
            }

            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)

            val body = builder.build()
            val jsonBody = json.encodeToString(body)

            val gmt = GMTDate(Date.from(Instant.now()).time)

            call.response.headers.append(HttpHeaders.LastModified, gmt.toHttpDate())
            call.response.headers.append(HttpHeaders.ETag, "\"${sha256Hex(jsonBody)}\"")

            call.respondText(jsonBody, contentType = ContentType.Application.Json)
        }

        get("/cache/{max-age}") {
            val maxAge = call.parameters["max-age"]

            if (maxAge == null || maxAge.any { !it.isDigit() }) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)

            val body = builder.build()
            val jsonBody = json.encodeToString(body)

            val gmt = GMTDate(Date.from(Instant.now()).time)

            call.response.headers.append(HttpHeaders.LastModified, gmt.toHttpDate())
            call.response.headers.append(HttpHeaders.ETag, "\"${sha256Hex(jsonBody)}\"")
            call.response.headers.append(HttpHeaders.CacheControl, "public, max-age=$maxAge")
            call.respondText(jsonBody, contentType = ContentType.Application.Json)
        }

        get("/etag/{etag}") {
            val noneMatch = call.request.headers[HttpHeaders.IfNoneMatch]
            val etag = call.parameters["etag"] ?: ""

            if (noneMatch != null && etag == noneMatch) {
                call.response.headers.append(HttpHeaders.ETag, "\"$etag\"")
                call.respond(HttpStatusCode.NotModified)
                return@get
            } else {
                val ifMatch = call.request.headers[HttpHeaders.IfMatch]
                if (ifMatch != null && ifMatch != etag) {
                    call.respond(HttpStatusCode.PreconditionFailed)
                    return@get
                }
            }

            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)

            call.response.headers.append(HttpHeaders.ETag, "\"$etag\"")
            call.respond(builder.build())
        }

        route("/response-headers") {
            handle {
                if (call.request.httpMethod != HttpMethod.Get && call.request.httpMethod != HttpMethod.Post) {
                    call.respond(HttpStatusCode.NotFound)
                    return@handle
                }

                val headers = mutableMapOf<String, List<String>>()
                val customHeaders = mutableListOf<Pair<String, String>>()

                for ((name, values) in call.request.queryParameters.entries()) {
                    if (values.isEmpty()) {
                        customHeaders.add(name to "")
                    } else {
                        for (v in values) {
                            customHeaders.add(name to v)
                        }
                    }

                    headers[name] = values
                }

                val invalidHeaders = customHeaders.filter { (name, _) -> !headerRegex.matches(name) }

                if (invalidHeaders.isNotEmpty()) {
                    call.respondText("Invalid HTTP header name: \"${invalidHeaders.first().first}\"", status = HttpStatusCode.BadRequest)
                    return@handle
                }

                headers["Content-Type"] = listOf("application/json")
                headers["Content-Length"] = listOf("0")
                val bytes = json.encodeToString(SmartValueMapSerializer, headers).toByteArray()
                headers["Content-Length"] = listOf((bytes.size + (bytes.size.toString().length - 1)).toString())

                for ((name, value) in customHeaders) {
                    call.response.headers.append(name, value)
                }

                call.respondText(
                    json.encodeToString(SmartValueMapSerializer, headers.toSortedMap()),
                    contentType = ContentType.Application.Json
                )
            }
        }
    }
}

private fun sha256Hex(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(input.toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }
}

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