package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
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

        val response = client.get("/cookies/delete")

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("/cookies", response.headers[HttpHeaders.Location])
    }
}