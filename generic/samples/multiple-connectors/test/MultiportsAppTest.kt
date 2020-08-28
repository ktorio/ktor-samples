import io.ktor.application.*
import io.ktor.http.*
import io.ktor.samples.multiports.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.*

/**
 * Note: TestApplicationRequest uses Host to determine the local endpoint
 */
class MultiportsAppTest {
    @Test
    fun testPublicApi(): Unit = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Get, "/") {
            addHeader("Host", "127.0.0.1:8080")
        }.apply {
            assertEquals("Connected to public api", response.content)
        }
    }

    @Test
    fun testPrivateApi(): Unit = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Get, "/") {
            addHeader("Host", "127.0.0.1:9090")
        }.apply {
            assertEquals("Connected to private api", response.content)
        }
    }
}
