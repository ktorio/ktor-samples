import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.samples.multiports.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.*

class MultiportsAppTest {
    @Test
    fun testApi(): Unit = testApplication {
        environment {
            envConfig()
        }
        client.get("/") {
            host = "0.0.0.0"
            port = 8080
        }.apply {
            assertEquals("Connected to public API", bodyAsText())
        }
        client.get("/") {
            host = "0.0.0.0"
            port = 9090
        }.apply {
            assertEquals("Connected to private API", bodyAsText())
        }
    }
}
