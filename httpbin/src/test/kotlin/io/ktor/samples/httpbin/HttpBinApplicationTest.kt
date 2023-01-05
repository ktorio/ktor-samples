package io.ktor.samples.httpbin

import io.ktor.client.request.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.*

/**
 * Tests the HttpBinApplication.
 */
class HttpBinApplicationTest {
    /**
     * Tests the redirect route by checking its behaviour.
     */
    @Test
    fun testRedirect() {
        testApplication {
            val client = createClient {
                followRedirects = false
            }
            client.get("/redirect/2").apply {
                assertEquals("/redirect/1", headers["Location"])
            }
            client.get("/redirect/1").apply {
                assertEquals("/redirect/0", headers["Location"])
            }
            client.get("/redirect/0").apply {
                assertEquals(null, headers["Location"])
            }
        }
    }
}
