package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class OriginTest {
    @Test
    fun test() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get").body<JsonObject>()
        assertEquals("localhost", body["origin"].asString)
    }
}