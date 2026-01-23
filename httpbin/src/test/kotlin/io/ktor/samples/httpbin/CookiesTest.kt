package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.setCookie
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import io.ktor.util.date.GMTDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CookiesTest {

    @Test
    fun cookiesNoHeaders() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cookies")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertNotNull(response.headers[HttpHeaders.ContentLength])

        val body = response.body<JsonObject>()
        assertTrue(body.get("cookies").asJsonObject.isEmpty)
    }

    @Test
    fun cookiesEmptyHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cookies") {
            header(HttpHeaders.Cookie, "")
        }
        val body = response.body<JsonObject>()
        assertTrue(body.get("cookies").asJsonObject.isEmpty)
    }

    @Test
    fun cookieSingle() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cookies") {
            header(HttpHeaders.Cookie, "name=value")
        }

        val body = response.body<JsonObject>()
        val cookies = body.get("cookies").asJsonObject
        assertEquals("value", cookies.get("name").asString)
    }

    @Test
    fun cookieOnlyName() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cookies") {
            header(HttpHeaders.Cookie, "name")
        }

        val body = response.body<JsonObject>()
        val cookies = body.get("cookies").asJsonObject
        assertEquals("", cookies.get("name").asString)
    }

    @Test
    fun cookiesMultipleSorted() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/cookies") {
            header(HttpHeaders.Cookie, "name1=value1; name3=value3; name2=value2")
        }

        val body = response.body<JsonObject>()
        val cookieNames = body.get("cookies").asJsonObject.keySet().iterator()
        assertEquals("name1", cookieNames.next())
        assertEquals("name2", cookieNames.next())
        assertEquals("name3", cookieNames.next())
        assertFalse(cookieNames.hasNext())
    }

    @Test
    fun cookiesDeleteNoQuery() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/delete")

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("/cookies", response.headers[HttpHeaders.Location])
    }

    @Test
    fun cookiesDeleteSingle() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/delete?name")
        val cookies = response.setCookie().iterator()

        val cookie = cookies.next()
        assertEquals("name", cookie.name)
        assertEquals("", cookie.value)
        assertEquals(GMTDate(0), cookie.expires)
        assertEquals(0, cookie.maxAge)
        assertEquals("/", cookie.path)

        assertFalse(cookies.hasNext())
    }

    @Test
    fun cookiesDeleteMultipleDuplicated() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/delete?a&b&a")
        val cookies = response.setCookie().iterator()

        val cookie1 = cookies.next()
        assertEquals("a", cookie1.name)
        assertEquals("", cookie1.value)
        assertEquals(GMTDate(0), cookie1.expires)
        assertEquals(0, cookie1.maxAge)
        assertEquals("/", cookie1.path)

        val cookie2 = cookies.next()
        assertEquals("b", cookie2.name)
        assertEquals("", cookie2.value)
        assertEquals(GMTDate(0), cookie2.expires)
        assertEquals(0, cookie2.maxAge)
        assertEquals("/", cookie2.path)

        assertFalse(cookies.hasNext())
    }

    @Test
    fun cookiesSetNoQuery() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/set")

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("/cookies", response.headers[HttpHeaders.Location])
    }

    @Test
    fun cookiesSetFew() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/set?a&b=2&c=3")
        val cookies = response.setCookie().iterator()
        assertEquals(Cookie("a", "", path = "/"), cookies.next())
        assertEquals(Cookie("b", "2", path = "/"), cookies.next())
        assertEquals(Cookie("c", "3", path = "/"), cookies.next())

        assertFalse(cookies.hasNext())
    }

    @Test
    fun cookiesSetDuplicated() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/set?a=1&a=2&a=3")
        val cookies = response.setCookie().iterator()
        assertEquals(Cookie("a", "1", path = "/"), cookies.next())

        assertFalse(cookies.hasNext())
    }

    @Test
    fun cookiesSetPath() = testApplication {
        application { module() }

        val client = createClient {
            followRedirects = false
        }

        val response = client.get("/cookies/set/name/value")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("/cookies", response.headers[HttpHeaders.Location])

        val cookies = response.setCookie().iterator()
        assertEquals(Cookie("name", "value", path = "/"), cookies.next())

        assertFalse(cookies.hasNext())
    }
}