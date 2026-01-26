package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnythingTest {

    @Test
    fun anything() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        for ((method, makeRequest) in listOf(
            HttpMethod.Get to suspend { client.get("/anything") },
            HttpMethod.Post to suspend { client.post("/anything") {} },
            HttpMethod.Patch to suspend { client.patch("/anything") },
            HttpMethod.Put to suspend { client.put("/anything") },
            HttpMethod.Delete to suspend { client.delete("/anything") },
        )) {
            val response = makeRequest()
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<JsonObject>()
            assertTrue(body.has("args"))
            assertTrue(body.has("data"))
            assertTrue(body.has("files"))
            assertTrue(body.has("form"))
            assertTrue(body.has("headers"))
            assertTrue(body.has("json"))
            assertTrue(body.has("origin"))
            assertTrue(body.has("url"))
            assertEquals(method.value, body.get("method").asString)
        }
    }

    @Test
    fun anythingWildCard() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        for ((method, makeRequest) in listOf(
            HttpMethod.Get to suspend { client.get("/anything/a/b/c") },
            HttpMethod.Post to suspend { client.post("/anything/e") {} },
            HttpMethod.Patch to suspend { client.patch("/anything/d/f/g/q/w") },
            HttpMethod.Put to suspend { client.put("/anything/p") },
            HttpMethod.Delete to suspend { client.delete("/anything/p") },
        )) {
            val response = makeRequest()
            val body = response.body<JsonObject>()
            assertEquals(method.value, body.get("method").asString)
        }
    }
}