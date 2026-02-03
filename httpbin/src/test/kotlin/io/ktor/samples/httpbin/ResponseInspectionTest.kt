package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResponseInspectionTest {
    @Test
    fun cacheNoHeaders() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cache")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )

        assertNotNull(response.headers[HttpHeaders.LastModified])
        assertNotNull(response.headers[HttpHeaders.ETag])

        val body = response.body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertTrue(body.has("url"))
    }

    @Test
    fun cacheWithIfModifiedSinceHeader() = testApplication {
        application { module() }

        val response = client.get("/cache") {
            header(HttpHeaders.IfModifiedSince, "anything")
        }
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertNull(response.contentType())
        assertEquals("", response.bodyAsText())
    }

    @Test
    fun cacheWithIfNoneMatchHeader() = testApplication {
        application { module() }

        val response = client.get("/cache") {
            header(HttpHeaders.IfNoneMatch, "anything")
        }
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertNull(response.contentType())
        assertEquals("", response.bodyAsText())
    }

    @Test
    fun cacheWithInvalidMaxAge() = testApplication {
        application { module() }

        val response = client.get("/cache/aaa")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun cacheWithMaxAge() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cache/123")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )

        assertNotNull(response.headers[HttpHeaders.LastModified])
        assertNotNull(response.headers[HttpHeaders.ETag])
        assertEquals("public, max-age=123", response.headers[HttpHeaders.CacheControl])

        val body = response.body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertTrue(body.has("url"))
    }

    @Test
    fun etagNoHeaders() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/etag/abcd")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )

        assertEquals("\"abcd\"", response.headers[HttpHeaders.ETag])

        val body = response.body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertTrue(body.has("url"))
    }

    @Test
    fun etagNotEqualIfNoneMatchHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/etag/abcd") {
            header(HttpHeaders.IfNoneMatch, "unmatched")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )

        assertEquals("\"abcd\"", response.headers[HttpHeaders.ETag])

        val body = response.body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertTrue(body.has("url"))
    }

    @Test
    fun etagEqualIfNoneMatchHeader() = testApplication {
        application { module() }

        val response = client.get("/etag/abcd") {
            header(HttpHeaders.IfNoneMatch, "abcd")
        }
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertEquals("\"abcd\"", response.headers[HttpHeaders.ETag])
    }


    @Test
    fun etagEqualIfMatchHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/etag/abcd") {
            header(HttpHeaders.IfMatch, "abcd")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )

        assertEquals("\"abcd\"", response.headers[HttpHeaders.ETag])

        val body = response.body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertTrue(body.has("url"))
    }

    @Test
    fun etagNotEqualIfMatchHeader() = testApplication {
        application { module() }

        val response = client.get("/etag/abcd") {
            header(HttpHeaders.IfMatch, "other")
        }
        assertEquals(HttpStatusCode.PreconditionFailed, response.status)
    }

    @Test
    fun responseHeadersDefaultGet() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/response-headers")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )
        val body = response.body<JsonObject>()
        assertEquals("application/json", body["Content-Type"].asString)
        assertEquals(response.contentType().toString(), body["Content-Type"].asString)
        assertEquals(
            response.contentLength().toString(),
            body["Content-Length"].asString
        )
    }

    @Test
    fun responseHeadersDefaultPost() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.post("/response-headers")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ContentType.Application.Json,
            response.contentType()?.withoutParameters()
        )
        val body = response.body<JsonObject>()
        assertEquals("application/json", body["Content-Type"].asString)
        assertEquals(response.contentType().toString(), body["Content-Type"].asString)
        assertEquals(
            response.contentLength().toString(),
            body["Content-Length"].asString
        )
    }

    @Test
    fun responseHeadersOnlyGetAndPost() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.MethodNotAllowed, client.put("/response-headers").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.patch("/response-headers").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.delete("/response-headers").status)
    }

    @Test
    fun responseHeadersCustomHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/response-headers?Header=value")
        val body = response.body<JsonObject>()
        assertEquals(
            response.contentLength().toString(),
            body["Content-Length"].asString
        )
        assertEquals("value", body["Header"].asString)
        assertEquals("value", response.headers["Header"])
    }

    @Test
    fun responseHeadersMultipleCustomSameName() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/response-headers?H1=v1&H1=v2&H1=v3")
        val body = response.body<JsonObject>()
        assertEquals(
            response.contentLength().toString(),
            body["Content-Length"].asString
        )

        val values = body["H1"].asJsonArray.iterator()
        assertEquals("v1", values.next().asString)
        assertEquals("v2", values.next().asString)
        assertEquals("v3", values.next().asString)
        assertFalse(values.hasNext())

        assertContentEquals(listOf("v1", "v2", "v3"), response.headers.getAll("H1"))
    }

    @Test
    fun responseHeadersAreSorted() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/response-headers?A=1&C=2&B=3")
        val body = response.body<JsonObject>()
        assertEquals(
            response.contentLength().toString(),
            body["Content-Length"].asString
        )

        val headers = body.keySet().iterator()
        assertEquals("A", headers.next())
        assertEquals("B", headers.next())
        assertEquals("C", headers.next())
        assertEquals("Content-Length", headers.next())
        assertEquals("Content-Type", headers.next())
    }

    @Test
    fun responseHeadersCustomEmpty() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/response-headers?No-Value")
        val body = response.body<JsonObject>()
        assertEquals(
            response.contentLength().toString(),
            body["Content-Length"].asString
        )

        assertEquals("", body["No-Value"].asString)
        assertEquals("", response.headers["No-Value"])
    }

    @Test
    fun responseHeadersInvalidHeaderNames() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.BadRequest, client.get("/response-headers?(").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/response-headers?)").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/response-headers?[").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/response-headers?]").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/response-headers?<").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/response-headers?>").status)

        assertEquals(
            "Invalid HTTP header name: \"(\"",
            client.get("/response-headers?(").bodyAsText()
        )
        assertEquals(
            "Invalid HTTP header name: \"<header>\"",
            client.get("/response-headers?<header>=value").bodyAsText()
        )
    }
}