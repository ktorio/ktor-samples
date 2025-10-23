package io.ktorgraal

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigureRoutingTest {

    @Test
    fun testGetHi() = testApplication {
        application { module() }
        val client = createClient {
            install(ContentNegotiation) { json() }
        }
        val response = client.get("/")
        val actual = response.body<JsonBody<String>>()
        assertEquals(JsonBody("Hello GraalVM!"), actual)
    }
}