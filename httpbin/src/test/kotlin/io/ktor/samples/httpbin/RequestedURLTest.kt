package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RequestedURLTest {
    @Test
    fun get() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get").body<JsonObject>()
        assertEquals("http://localhost/get", body["url"].asString)
    }

    @Test
    fun query() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get?abc").body<JsonObject>()
        assertEquals("http://localhost/get?abc", body["url"].asString)
    }

    @Test
    fun hostHeader() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get") {
            header(HttpHeaders.Host, "httpbin.org:3333")
        }.body<JsonObject>()
        assertEquals("http://httpbin.org:3333/get", body["url"].asString)
    }
}