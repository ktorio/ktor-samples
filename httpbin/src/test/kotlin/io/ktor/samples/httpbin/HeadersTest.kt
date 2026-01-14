package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.gson.gson
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.testing.testApplication
import io.ktor.util.pipeline.PipelinePhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeadersTest {
    @Test
    fun defaultHeaders() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get").body<JsonObject>()
        assertTrue(body.has("headers"))
        val headers = body["headers"].asJsonObject
        // From the ContentNegotiation plugin
        assertEquals("application/json", headers["Accept"].asString)
        assertEquals("UTF-8", headers["Accept-Charset"].asString)
        // From the test client engine
        assertEquals("0", headers["Content-Length"].asString)
        assertEquals("ktor-client", headers["User-Agent"].asString)
    }

    @Test
    fun customHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get") {
            header("Custom", "value")
        }.body<JsonObject>()

        assertTrue(body.has("headers"))
        val headers = body["headers"].asJsonObject
        assertEquals("value", headers["Custom"].asString)
    }

    @Test
    fun multipleValueForOneHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get") {
            header("Header", "1")
            header("Header", "2")
            header("Header", "3")
        }.body<JsonObject>()

        assertTrue(body.has("headers"))
        val headers = body["headers"].asJsonObject
        assertEquals("1,2,3", headers["Header"].asString)
    }

    @Test
    fun headersAreSorted() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get").body<JsonObject>()
        val headers = body["headers"].asJsonObject
        val names = headers.keySet().iterator()

        assertEquals("Accept", names.next())
        assertEquals("Accept-Charset", names.next())
        assertEquals("Content-Length", names.next())
        assertEquals("User-Agent", names.next())
    }
}