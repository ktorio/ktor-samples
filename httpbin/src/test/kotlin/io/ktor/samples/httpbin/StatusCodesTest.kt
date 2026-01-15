package io.ktor.samples.httpbin

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusCodesTest {
    @Test
    fun get() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.OK, client.get("/status/200").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/status/404").status)
        assertEquals(HttpStatusCode.InternalServerError, client.get("/status/500").status)
        assertEquals(HttpStatusCode.OK, client.head("/get").status)

        val noRedirectClient = createClient {
            followRedirects = false
        }

        for (code in listOf("301", "302", "303", "307")) {
            assertEquals(
                "/redirect/1",
                noRedirectClient.get("/status/$code").headers["Location"]
            )
        }
    }

    @Test
    fun post() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.OK, client.post("/status/200").status)
        assertEquals(HttpStatusCode.NotFound, client.post("/status/404").status)
        assertEquals(HttpStatusCode.InternalServerError, client.post("/status/500").status)

        val noRedirectClient = createClient {
            followRedirects = false
        }

        for (code in listOf("301", "302", "303", "307")) {
            assertEquals(
                "/redirect/1",
                noRedirectClient.post("/status/$code").headers["Location"]
            )
        }
    }

    @Test
    fun put() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.OK, client.put("/status/200").status)
        assertEquals(HttpStatusCode.NotFound, client.put("/status/404").status)
        assertEquals(HttpStatusCode.InternalServerError, client.put("/status/500").status)

        val noRedirectClient = createClient {
            followRedirects = false
        }

        for (code in listOf("301", "302", "303", "307")) {
            assertEquals(
                "/redirect/1",
                noRedirectClient.put("/status/$code").headers["Location"]
            )
        }
    }

    @Test
    fun patch() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.OK, client.patch("/status/200").status)
        assertEquals(HttpStatusCode.NotFound, client.patch("/status/404").status)
        assertEquals(HttpStatusCode.InternalServerError, client.patch("/status/500").status)

        val noRedirectClient = createClient {
            followRedirects = false
        }

        for (code in listOf("301", "302", "303", "307")) {
            assertEquals(
                "/redirect/1",
                noRedirectClient.patch("/status/$code").headers["Location"]
            )
        }
    }

    @Test
    fun delete() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.OK, client.delete("/status/200").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/status/404").status)
        assertEquals(HttpStatusCode.InternalServerError, client.delete("/status/500").status)

        val noRedirectClient = createClient {
            followRedirects = false
        }

        for (code in listOf("301", "302", "303", "307")) {
            assertEquals(
                "/redirect/1",
                noRedirectClient.delete("/status/$code").headers["Location"]
            )
        }
    }

    @Test
    fun invalidStatusCode() = testApplication {
        application { module() }

        val response = client.get("/status/invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("text/html; charset=UTF-8", response.contentType().toString())
        assertEquals("Invalid status code", response.bodyAsText())
    }

    @Test
    fun randomStatusCode() = testApplication {
        application { module(Random(42)) }

        val response = client.get("/status/204,404,500")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}