package io.ktor.samples.httpbin

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageTest {

    @Test
    fun imageNoAccept() = testApplication {
        application { module() }

        val response = client.get("/image")
        assertEquals(HttpStatusCode.NotAcceptable, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
    }

    @Test
    fun imageUnsupportedType() = testApplication {
        application { module() }

        val response = client.get("/image") {
            header(HttpHeaders.Accept, "image/bmp,application/json")
        }
        assertEquals(HttpStatusCode.NotAcceptable, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
    }

    @Test
    fun imageAcceptDifferentTypes() = testApplication {
        application { module() }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/webp")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/svg+xml")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/svg+xml; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/jpeg")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/jpeg", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/png")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/png", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/*")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/png", response.headers[HttpHeaders.ContentType])
        }
    }

    @Test
    fun imageJpeg() = testApplication {
        application { module() }

        val response = client.get("/image/jpeg")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("image/jpeg", response.headers[HttpHeaders.ContentType])
    }

    @Test
    fun imagePng() = testApplication {
        application { module() }

        val response = client.get("/image/png")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("image/png", response.headers[HttpHeaders.ContentType])
    }

    @Test
    fun imageSvg() = testApplication {
        application { module() }

        val response = client.get("/image/svg")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("image/svg+xml; charset=UTF-8", response.headers[HttpHeaders.ContentType])
    }

    @Test
    fun imageWebp() = testApplication {
        application { module() }

        val response = client.get("/image/webp")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
    }
}