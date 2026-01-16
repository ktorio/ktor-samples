package io.ktor.samples.httpbin

import com.aayushatharva.brotli4j.Brotli4jLoader
import com.aayushatharva.brotli4j.decoder.Decoder
import com.aayushatharva.brotli4j.decoder.DecoderJNI
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import io.ktor.util.DeflateEncoder
import io.ktor.util.GZipEncoder
import io.ktor.utils.io.toByteArray
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResponseFormatTest {
    @Test
    fun brotli() = testApplication {
        application { module() }

        val response = client.get("/brotli")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("br", response.headers[HttpHeaders.ContentEncoding])
        assertEquals("application/json", response.headers[HttpHeaders.ContentType])

        Brotli4jLoader.ensureAvailability()
        val directDecompress = Decoder.decompress(response.body<ByteArray>())

        assertEquals(directDecompress.resultStatus, DecoderJNI.Status.DONE)

        val body = Gson().fromJson(
            directDecompress.decompressedData.toString(Charsets.UTF_8),
            JsonObject::class.java
        )

        assertEquals(true, body.get("brotli").asBoolean)
        assertEquals("GET", body.get("method").asString)
        assertEquals("localhost", body.get("origin").asString)

        val headers = body.get("headers").asJsonObject.keySet().iterator()
        assertEquals("Accept", headers.next())
        assertEquals("Accept-Charset", headers.next())
        assertEquals("Content-Length", headers.next())
        assertEquals("User-Agent", headers.next())
        assertFalse(headers.hasNext())
    }

    @Test
    fun deflate() = testApplication {
        application { module() }

        val response = client.get("/deflate")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("deflate", response.headers[HttpHeaders.ContentEncoding])
        assertEquals("application/json", response.headers[HttpHeaders.ContentType])

        val decodedChannel = DeflateEncoder.decode(response.body())

        val body = Gson().fromJson(
            decodedChannel.toByteArray().toString(Charsets.UTF_8),
            JsonObject::class.java
        )

        assertEquals(true, body.get("deflated").asBoolean)
        assertEquals("GET", body.get("method").asString)
        assertEquals("localhost", body.get("origin").asString)

        val headers = body.get("headers").asJsonObject.keySet().iterator()
        assertEquals("Accept", headers.next())
        assertEquals("Accept-Charset", headers.next())
        assertEquals("Content-Length", headers.next())
        assertEquals("User-Agent", headers.next())
        assertFalse(headers.hasNext())
    }

    @Test
    fun deny() = testApplication {
        application { module() }

        val response = client.get("/deny")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/plain; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertContains(response.bodyAsText(), "YOU SHOULDN'T BE HERE")
    }

    @Test
    fun utf8Encoding() = testApplication {
        application { module() }

        val response = client.get("/encoding/utf8")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertTrue(isValidUtf8(response.body()))
    }

    @Test
    fun gzip() = testApplication {
        application { module() }

        val response = client.get("/gzip")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("gzip", response.headers[HttpHeaders.ContentEncoding])
        assertEquals("application/json", response.headers[HttpHeaders.ContentType])

        val decodedChannel = GZipEncoder.decode(response.body())

        val body = Gson().fromJson(
            decodedChannel.toByteArray().toString(Charsets.UTF_8),
            JsonObject::class.java
        )

        assertEquals(true, body.get("gzipped").asBoolean)
        assertEquals("GET", body.get("method").asString)
        assertEquals("localhost", body.get("origin").asString)

        val headers = body.get("headers").asJsonObject.keySet().iterator()
        assertEquals("Accept", headers.next())
        assertEquals("Accept-Charset", headers.next())
        assertEquals("Content-Length", headers.next())
        assertEquals("User-Agent", headers.next())
        assertFalse(headers.hasNext())
    }

    @Test
    fun html() = testApplication {
        application { module() }

        val response = client.get("/html")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertContains(response.bodyAsText(), "<!DOCTYPE html>")
    }

    @Test
    fun json() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/json")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertTrue(response.body<JsonElement>().isJsonObject)
    }

    @Test
    fun robots() = testApplication {
        application { module() }

        val response = client.get("/robots.txt")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/plain; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertContains(response.bodyAsText(), "Disallow: /deny")
    }

    @Test
    fun xml() = testApplication {
        application { module() }

        val response = client.get("/xml")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/xml; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertContains(response.bodyAsText(), "<?xml version='1.0'")
    }

    private fun isValidUtf8(payload: ByteArray): Boolean {
        val decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)

        return try {
            decoder.decode(ByteBuffer.wrap(payload))
            true
        } catch (_: CharacterCodingException) {
            false
        }
    }
}