package io.ktor.samples.httpbin

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RedirectsTest {
    @Test
    fun absolute() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/absolute-redirect/0")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun absolute1() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/absolute-redirect/1")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("http://localhost/get", response.headers["Location"])
    }

    @Test
    fun absolute2() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/absolute-redirect/2")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("http://localhost/absolute-redirect/1", response.headers["Location"])
    }

    @Test
    fun redirectToNoQuery() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect-to")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun redirectToRelativeUrl() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect-to?url=/get")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/get", response.headers["Location"])
    }

    @Test
    fun redirectToSpecialSymbols() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect-to?url=/ ()")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/ ()", response.headers["Location"])
    }

    @Test
    fun redirectToAbsoluteUrl() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect-to?url=https://google.com?a=b")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("https://google.com?a=b", response.headers["Location"])
    }

    @Test
    fun redirectToInvalidStatus() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect-to?url=/&status_code=999")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/", response.headers["Location"])
    }

    @Test
    fun redirectToCustomStatus() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect-to?url=/&status_code=307")
        assertEquals(HttpStatusCode.TemporaryRedirect, response.status)
        assertEquals("/", response.headers["Location"])
    }

    @Test
    fun redirectToOtherMethods() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        for (makeRequest in listOf(
            suspend { client.post("/redirect-to?url=/&status_code=307") },
            suspend { client.patch("/redirect-to?url=/&status_code=307") },
            suspend { client.put("/redirect-to?url=/&status_code=307") },
            suspend { client.delete("/redirect-to?url=/&status_code=307") },
        )) {
            val response = makeRequest()
            assertEquals(HttpStatusCode.TemporaryRedirect, response.status)
            assertEquals("/", response.headers["Location"])
        }
    }

    @Test
    fun redirect() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect/0")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun redirect1() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect/1")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/get", response.headers["Location"])
    }

    @Test
    fun redirect2() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/redirect/2")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/relative-redirect/1", response.headers["Location"])
    }

    @Test
    fun relativeRedirect() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/relative-redirect/0")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun relativeRedirect1() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/relative-redirect/1")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/get", response.headers["Location"])
    }

    @Test
    fun relativeRedirect2() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/relative-redirect/2")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/relative-redirect/1", response.headers["Location"])
    }
}