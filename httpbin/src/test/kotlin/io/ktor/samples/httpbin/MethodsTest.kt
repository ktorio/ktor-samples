package io.ktor.samples.httpbin

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MethodsTest {
    @Test
    fun get() = testApplication {
        application { module() }

        client.get("/get").let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }

        assertEquals(HttpStatusCode.MethodNotAllowed, client.post("/get").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.patch("/get").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.put("/get").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.delete("/get").status)

        assertEquals(HttpStatusCode.OK, client.head("/get").status)
    }

    @Test
    fun post() = testApplication {
        application { module() }

        client.post("/post").let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }

        assertEquals(HttpStatusCode.MethodNotAllowed, client.get("/post").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.patch("/post").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.put("/post").status)
        assertEquals(HttpStatusCode.MethodNotAllowed, client.delete("/post").status)
    }

    @Test
    fun put() = testApplication {
        application { module() }

        client.put("/put").let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }
    }

    @Test
    fun delete() = testApplication {
        application { module() }

        client.delete("/delete").let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }
    }

    @Test
    fun patch() = testApplication {
        application { module() }

        client.patch("/patch").let { response ->
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        }
    }
}