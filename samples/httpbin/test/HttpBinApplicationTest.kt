import io.ktor.application.*
import io.ktor.http.*
import io.ktor.samples.httpbin.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
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
        testRequest(HttpMethod.Get, "/redirect/2") { assertEquals("/redirect/1", response.headers["Location"]) }
        testRequest(HttpMethod.Get, "/redirect/1") { assertEquals("/redirect/0", response.headers["Location"]) }
        testRequest(HttpMethod.Get, "/redirect/0") { assertEquals(null, response.headers["Location"]) }
    }
}

private fun testRequest(
    method: HttpMethod,
    uri: String,
    setup: suspend TestApplicationRequest.() -> Unit = {},
    checks: suspend TestApplicationCall.() -> Unit
) {
    httpBinTest {
        val req = handleRequest(method, uri) { runBlocking { setup() } }
        checks(req)
    }
}

private fun httpBinTest(callback: suspend TestApplicationEngine.() -> Unit): Unit {
    withTestApplication(Application::main) {
        runBlocking { callback() }
    }
}
