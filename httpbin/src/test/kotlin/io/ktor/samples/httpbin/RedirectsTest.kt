package io.ktor.samples.httpbin

import io.ktor.client.request.get
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
}