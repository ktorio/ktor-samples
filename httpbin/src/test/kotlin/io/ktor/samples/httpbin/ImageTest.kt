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

        val client = createClient {
            useDefaultTransformers = false
        }
        val response = client.get("/image")
        assertEquals(HttpStatusCode.NotAcceptable, response.status)
    }

    @Test
    fun imageUnsupportedType() = testApplication {
        application { module() }

        val client = createClient {
            useDefaultTransformers = false
        }

        val response = client.get("/image") {
            header(HttpHeaders.Accept, "image/bmp,application/xml")
        }
        assertEquals(HttpStatusCode.NotAcceptable, response.status)
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
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }
    }

    @Test
    fun imageQualityCases() = testApplication {
        application { module() }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/*")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/jpeg")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/jpeg", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/jpeg;q=0.8,image/*")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/jpeg;q=0.8,image/*;q=0.8")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/jpeg", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/webp;q=1.0,image/*;q=0.9")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/png;q=0.9,image/jpeg;q=0.9")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/png", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/png;q=0.9,image/jpeg;q=0.9,/;q=0.1")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/png", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/jpeg;q=0.8,image/*;q=1.0,image/jpeg;q=0.2")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/svg+xml;q=0.5,image/*;q=0.4")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/svg+xml; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/avif,image/webp;q=0.9,image/*;q=0.8")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/*;q=0.0,image/*;q=0.1")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/webp", response.headers[HttpHeaders.ContentType])
        }

        client.get("/image") {
            header(HttpHeaders.Accept, "image/jpeg;q=1.0,image/jpeg;q=1.0")
        }.let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("image/jpeg", response.headers[HttpHeaders.ContentType])
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