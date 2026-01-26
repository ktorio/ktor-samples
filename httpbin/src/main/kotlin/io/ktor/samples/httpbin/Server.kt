package io.ktor.samples.httpbin

import com.aayushatharva.brotli4j.Brotli4jLoader
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.samples.httpbin.SampleModel.Slide
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.ConnectorType
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import io.ktor.util.DeflateEncoder
import io.ktor.util.GZipEncoder
import io.ktor.util.date.*
import io.ktor.utils.io.*
import io.ktor.utils.io.writeByte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import kotlin.String
import kotlin.collections.List
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
private val prettyJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

private val headerRegex = """[A-Za-z0-9-_]+""".toRegex()

private val UNSAFE_METHODS = setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Delete)

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
fun Application.module(random: Random = Random.Default) {
    install(DefaultHeaders)
    install(AutoHeadResponse)
    install(ContentNegotiation) {
        json(prettyJson)
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

        for ((method, path) in listOf(
            HttpMethod.Post to "/post",
            HttpMethod.Put to "/put",
            HttpMethod.Patch to "/patch",
            HttpMethod.Delete to "/delete",
        )) {
            route(path) {
                method(method) {
                    handle {
                        val builder = HttpbinResponse.Builder()
                            .argsFromQuery(call.request.queryParameters)
                            .setHeaders(call.request.headers)
                            .setOrigin(call.request.local)
                            .setURL(call.request)
                            .makeUnsafe()
                            .loadBody(call)

                        call.respond(builder.build())
                    }
                }
            }
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
            call.respond(HeadersResponse(call.request.headers.toSortedMap()))
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
            val jsonBody = prettyJson.encodeToString(body)

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
            val jsonBody = prettyJson.encodeToString(body)

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
                val bytes = prettyJson.encodeToString(SmartValueMapSerializer, headers).toByteArray()
                headers["Content-Length"] = listOf((bytes.size + (bytes.size.toString().length - 1)).toString())

                for ((name, value) in customHeaders) {
                    call.response.headers.append(name, value)
                }

                call.respondText(
                    prettyJson.encodeToString(SmartValueMapSerializer, headers.toSortedMap()),
                    contentType = ContentType.Application.Json
                )
            }
        }

        get("/brotli") {
            val body = prettyJson.encodeToString(
                BrotliResponse(
                    brotli = true,
                    headers = call.request.headers.toSortedMap(),
                    method = call.request.httpMethod.value,
                    origin = call.request.local.remoteAddress
                )
            )

            Brotli4jLoader.ensureAvailability()
            val compressed = com.aayushatharva.brotli4j.encoder.Encoder.compress(body.toByteArray(Charsets.UTF_8))
            call.response.headers.append(HttpHeaders.ContentEncoding, "br")
            call.respondBytes(compressed, contentType = ContentType.Application.Json)
        }

        get("/deflate") {
            val body = prettyJson.encodeToString(
                DeflateResponse(
                    deflated = true,
                    headers = call.request.headers.toSortedMap(),
                    method = call.request.httpMethod.value,
                    origin = call.request.local.remoteAddress
                )
            )

            val compressed = DeflateEncoder.encode(ByteReadChannel(body))

            call.response.headers.append(HttpHeaders.ContentEncoding, "deflate")
            call.respondBytes(compressed.toByteArray(), contentType = ContentType.Application.Json)
        }

        get("/gzip") {
            val body = prettyJson.encodeToString(
                GzipResponse(
                    gzipped = true,
                    headers = call.request.headers.toSortedMap(),
                    method = call.request.httpMethod.value,
                    origin = call.request.local.remoteAddress
                )
            )

            val compressed = GZipEncoder.encode(ByteReadChannel(body))

            call.response.headers.append(HttpHeaders.ContentEncoding, "gzip")
            call.respondBytes(compressed.toByteArray(), contentType = ContentType.Application.Json)
        }

        get("/deny") {
            call.respondText(DENY_ASCII, contentType = ContentType.Text.Plain)
        }

        get("/encoding/utf8") {
            call.respondResource("utf8.html")
        }

        get("/html") {
            call.respondResource("sample.html")
        }

        get("/xml") {
            call.respondResource("sample.xml")
        }

        get("/json") {
            call.respond(
                SampleModel(
                    SampleModel.SlideShow(
                        author = "Yours Truly",
                        date = "date of publication",
                        slides = listOf(
                            Slide("Wake up to WonderWidgets!", "all"),
                            Slide(
                                "Overview", "all", listOf(
                                    "Why <em>WonderWidgets</em> are great",
                                    "Who <em>buys</em> WonderWidgets"
                                )
                            )
                        ),
                        title = "Sample Slide Show"
                    )
                )
            )
        }

        get("/robots.txt") {
            call.respondText("User-agent: *\nDisallow: /deny", contentType = ContentType.Text.Plain)
        }

        get("/base64/{value}") {
            val value = call.parameters["value"] ?: return@get
            try {
                val decoded = Base64.UrlSafe.decode(value)
                call.respondBytes(decoded, contentType = ContentType.Text.Html.withCharset(Charsets.UTF_8))
            } catch (_: IllegalArgumentException) {
                call.respondText(
                    "Incorrect Base64 data try: SFRUUEJJTiBpcyBhd2Vzb21l",
                    contentType = ContentType.Text.Html
                )
            }
        }

        get("/bytes/{n}") {
            val n = call.parameters["n"]?.toIntOrNull()

            if (n == null || n < 0) {
                return@get
            }

            val seedValue = call.queryParameters["seed"]

            val random = if (seedValue == null) {
                Random.Default
            } else {
                val seed = seedValue.toLongOrNull()
                if (seed == null) {
                    call.respondText("Invalid seed. Expected an integer.", status = HttpStatusCode.BadRequest)
                    return@get
                }
                Random(seed)
            }

            call.respondBytes(random.nextBytes(n.coerceIn(0, 100 * 1024)), contentType = ContentType.Application.OctetStream)
        }

        route("/delay/{duration_sec}") {
            get {
                val sec = call.parameters["duration_sec"]?.toIntOrNull() ?: return@get
                delay((sec * 1000L).coerceIn(0, 10 * 1000))

                val builder = HttpbinResponse.Builder()
                    .argsFromQuery(call.request.queryParameters)
                    .setHeaders(call.request.headers)
                    .setOrigin(call.request.local)
                    .setURL(call.request)

                call.respond(builder.build())
            }

            for (method in UNSAFE_METHODS) {
                method(method) {
                    handle {
                        val sec = call.parameters["duration_sec"]?.toIntOrNull() ?: return@handle
                        delay((sec * 1000L).coerceIn(0, 10 * 1000))

                        val builder = HttpbinResponse.Builder()
                            .argsFromQuery(call.request.queryParameters)
                            .setHeaders(call.request.headers)
                            .setOrigin(call.request.local)
                            .setURL(call.request)
                            .makeUnsafe()
                            .loadBody(call)

                        call.respond(builder.build())
                    }
                }
            }
        }

        fun timeMillis(): Float {
            return System.nanoTime() / 1000_000f
        }

        get("/drip") {
            val initDelaySec = call.queryParameters["delay"]?.toLongOrNull() ?: 2
            delay(initDelaySec.coerceIn(0, 10) * 1000)

            val durationSec = call.queryParameters["duration"]?.toLongOrNull() ?: 2
            val numBytes = (call.queryParameters["numbytes"]?.toIntOrNull() ?: 10).coerceIn(0, 100 * 1024)

            val status = call.queryParameters["status"]?.toIntOrNull() ?: 200
            val statusCode = if (status in 100..599) HttpStatusCode.fromValue(status) else HttpStatusCode.OK

            call.respondBytesWriter(status = statusCode) {
                drip(numBytes, durationSec.coerceIn(0, 10) * 1000, ::timeMillis).collect { event ->
                    when (event) {
                        is Delay -> {
                            delay(event.duration)
                        }
                        is Bytes -> {
                            for (i in 0 ..< event.numBytes) {
                                writeByte('*'.code.toByte())
                            }
                            flush()
                        }
                    }
                }
            }
        }

        get("/links/{n}/{offset}") {
            val number = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(1)
            val active = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

            val links = buildString {
                for (i in 0..<number) {
                    if (i == active) {
                        append("$i ")
                        continue
                    }

                    append("<a href='/links/$number/$i'>$i</a> ")
                }
            }

            call.respondText(
                "<html><head><title>Links</title></head><body>$links</body></html>",
                contentType = ContentType.Text.Html
            )
        }

        route("/range/{number}") {
            install(PartialContent)
            get {
                val number = (call.parameters["number"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

                call.response.headers.append(HttpHeaders.ETag, "range$number")
                call.response.headers.append(HttpHeaders.AcceptRanges, "bytes")

                if (number == 0) {
                    call.respondText(
                        "number of bytes must be in the range (0, 102400]",
                        status = HttpStatusCode.BadRequest,
                        contentType = ContentType.Text.Html
                    )
                    return@get
                }


                val alphabet = ('a'..'z')
                val body = alphabet.joinToString(separator = "").repeat(number / 26) +
                        alphabet.take(number % 26).joinToString(separator = "")

                call.respond(
                    object : OutgoingContent.ReadChannelContent() {
                        override val contentType: ContentType = ContentType.Application.OctetStream
                        override val contentLength: Long = body.length.toLong()
                        override fun readFrom(): ByteReadChannel {
                            return ByteReadChannel(body)
                        }
                    }
                )
            }
        }

        get("/stream-bytes/{n}") {
            val number = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceIn(0, 100 * 1024)
            val chunkSize = (call.request.queryParameters["chunk_size"]?.toIntOrNull() ?: (10 * 1024)).coerceAtLeast(1)

            val seedValue = call.queryParameters["seed"]

            val random = if (seedValue == null) {
                Random.Default
            } else {
                val seed = seedValue.toLongOrNull()
                if (seed == null) {
                    call.respondText("Invalid seed. Expected an integer.", status = HttpStatusCode.BadRequest)
                    return@get
                }
                Random(seed)
            }

            val bytes = random.nextBytes(number)

            call.respondBytesWriter(contentType = ContentType.Application.OctetStream) {
                var written = 0
                for (i in 0..<(number / chunkSize)) {
                    written += chunkSize
                    writeFully(bytes.sliceArray(i * chunkSize ..< (i + 1) * chunkSize))
                }

                writeFully(bytes.sliceArray(written ..< number))
            }
        }

        get("/stream/{n}") {
            val number = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

            val builder = HttpbinResponse.Builder()
                .argsFromQuery(call.request.queryParameters)
                .setHeaders(call.request.headers)
                .setOrigin(call.request.local)
                .setURL(call.request)

            call.respondTextWriter(contentType = ContentType.Application.Json) {
                for (id in 0 until number) {
                    val payload = Json.encodeToString(builder.setID(id).build())
                    append(payload)
                    append("\n")
                    flush()
                }
            }
        }

        get("/uuid") {
            call.respond(UuidResponse(Uuid.random().toString()))
        }

        get("/cookies") {
            call.respond(CookiesResponse(call.request.cookies.rawCookies.toSortedMap()))
        }

        get("/cookies/delete") {
            call.response.headers.append(HttpHeaders.Location, "/cookies")
            call.response.headers.append(
                HttpHeaders.ContentType,
                ContentType.Text.Html.withCharset(Charsets.UTF_8).toString()
            )

            for (name in call.queryParameters.names()) {
                call.response.cookies.append(
                    name = name,
                    value = "",
                    expires = GMTDate(0),
                    maxAge = 0,
                    path = "/",
                )
            }

            call.respond(HttpStatusCode.Found)
        }

        get("/cookies/set") {
            call.response.headers.append(HttpHeaders.Location, "/cookies")
            call.response.headers.append(
                HttpHeaders.ContentType,
                ContentType.Text.Html.withCharset(Charsets.UTF_8).toString()
            )

            for (name in call.queryParameters.names()) {
                call.response.cookies.append(
                    name = name,
                    value = call.queryParameters[name] ?: "",
                    path = "/",
                )
            }

            call.respond(HttpStatusCode.Found)
        }

        get("/cookies/set/{name}/{value}") {
            call.response.headers.append(HttpHeaders.Location, "/cookies")
            call.response.headers.append(
                HttpHeaders.ContentType,
                ContentType.Text.Html.withCharset(Charsets.UTF_8).toString()
            )

            call.response.cookies.append(
                name = call.parameters.getOrFail("name"),
                value = call.parameters.getOrFail("value"),
                path = "/",
            )

            call.respond(HttpStatusCode.Found)
        }

        get("/image") {
            val serverAccepts = listOf("image/webp", "image/svg+xml", "image/jpeg", "image/png", "image/*")

            val clientAccepts = call.request.accept()
                ?.split(",")
                ?.map(String::trim) ?: emptyList()

            val accepts = clientAccepts.intersect(serverAccepts)

            if (accepts.isEmpty()) {
                call.respond(
                    HttpStatusCode.NotAcceptable,
                    ImageErrorResponse(
                        message = "Client did not request a supported media type",
                        accept = serverAccepts
                    ),
                )
                return@get
            }

            when (accepts.first()) {
                "image/webp" -> {
                    call.respondResource("sample.webp")
                }
                "image/svg+xml" -> {
                    call.respondResource("sample.svg")
                }
                "image/jpeg" -> {
                    call.respondResource("sample.jpg")
                }
                "image/png", "image/*" -> {
                    call.respondResource("sample.png")
                }
            }
        }

        get("/image/jpeg") {
            call.respondResource("sample.jpg")
        }
        get("/image/png") {
            call.respondResource("sample.png")
        }
        get("/image/svg") {
            call.respondResource("sample.svg")
        }
        get("/image/webp") {
            call.respondResource("sample.webp")
        }

        get("/absolute-redirect/{n}") {
            val n = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

            if (n == 0) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val connectors = engine.resolvedConnectors()
            require(connectors.isNotEmpty())
            with(connectors.first()) {
                val proto = if (type == ConnectorType.HTTPS) "https" else "http"
                val portPart = when (port) {
                    80 if type == ConnectorType.HTTP -> ""
                    443 if type == ConnectorType.HTTPS -> ""
                    else -> ":$port"
                }

                val baseUrl = "$proto://$host$portPart"
                call.respondRedirect(baseUrl + if (n <= 1) "/get" else "/absolute-redirect/${n-1}")
            }
        }

        route("/redirect-to") {
            handle {
                val url = call.queryParameters["url"]

                if (url == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@handle
                }

                var statusCode = call.request.queryParameters["status_code"]?.toIntOrNull() ?: 302
                if (statusCode !in 100..599) {
                    statusCode = 302
                }

                call.response.headers.append(HttpHeaders.Location, url)
                call.respond(HttpStatusCode.fromValue(statusCode))
            }
        }

        get("/redirect/{n}") {
            val n = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

            if (n == 0) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            call.respondRedirect(if (n <= 1) "/get" else "/relative-redirect/${n-1}")
        }

        get("/relative-redirect/{n}") {
            val n = (call.parameters["n"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

            if (n == 0) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            call.respondRedirect(if (n <= 1) "/get" else "/relative-redirect/${n-1}")
        }
    }
}

sealed interface DripEvent
data class Delay(val duration: Long): DripEvent
data class Bytes(val numBytes: Int): DripEvent

fun drip(totalBytes: Int, durationMs: Long, timeMillis: () -> Float): Flow<DripEvent> = flow {
    var numBytes = totalBytes
    var duration = durationMs.toFloat()
    val minDelay = 10.0f

    while (true) {
        if (numBytes <= 0) {
            if (duration > 0) emit(Delay(duration.toLong()))
            break
        }
        if (duration <= 0) {
            emit(Bytes(numBytes))
            break
        }

        var durationPerBytes = duration / numBytes
        var bytesToWrite = 1
        if (durationPerBytes < minDelay) {
            bytesToWrite = (minDelay.coerceAtMost(duration) / durationPerBytes).toInt()
            durationPerBytes = minDelay.coerceAtMost(duration)
        }

        val start = timeMillis()
        emit(Bytes(bytesToWrite))

        val elapsed = timeMillis() - start

        if (elapsed <= durationPerBytes) {
            if (numBytes - bytesToWrite <= 0) {
                emit(Delay((duration - elapsed).toLong()))
                break
            }

            emit(Delay((durationPerBytes - elapsed).toLong()))
        }

        duration -= (timeMillis() - start)
        numBytes -= bytesToWrite
    }
}

private fun sha256Hex(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(input.toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }
}

private fun Headers.toSortedMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for ((key, values) in entries()) {
        map[key] = values.joinToString(separator = ",")
    }
    return map.toSortedMap()
}

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
    val origin: String,
    val url: String,
    val id: Int? = null
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
                id = id
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