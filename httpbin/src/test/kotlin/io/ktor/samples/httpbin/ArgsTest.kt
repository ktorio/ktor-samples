package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArgsTest {
    @Test
    fun noArgs() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get").body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.get("args").asJsonObject.isEmpty)
    }

    @Test
    fun keyValueArg() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get?a=b").body<JsonObject>()
        assertTrue(body.has("args"))
        val args = body.get("args").asJsonObject
        assertEquals("b", args["a"].asString)
    }

    @Test
    fun noValueArg() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get?a").body<JsonObject>()
        assertTrue(body.has("args"))
        val args = body.get("args").asJsonObject
        assertEquals("", args["a"].asString)
    }

    @Test
    fun multipleValueForOneArg() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get?a=1&a=2").body<JsonObject>()
        assertTrue(body.has("args"))
        val args = body.get("args").asJsonObject
        val values = args["a"].asJsonArray.iterator()
        assertEquals("1", values.next().asString)
        assertEquals("2", values.next().asString)
        assertFalse(values.hasNext())
    }

    @Test
    fun multipleArgs() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/get?name=value&a=&b=42").body<JsonObject>()
        assertTrue(body.has("args"))
        val args = body.get("args").asJsonObject
        assertEquals("value", args["name"].asString)
        assertEquals("", args["a"].asString)
    }
}