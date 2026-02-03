package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.withCharset
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.method
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ExperimentalKtorApi
import io.ktor.utils.io.writeByte
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.text.toIntOrNull
import kotlin.text.toLongOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val UNSAFE_METHODS = setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Delete)

@OptIn(ExperimentalUuidApi::class, ExperimentalKtorApi::class)
fun Route.dynamic() {
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
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
    }.describe {
        tag("Dynamic data")
    }

    get("/uuid") {
        call.respond(UuidResponse(Uuid.random().toString()))
    }.describe {
        tag("Dynamic data")
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