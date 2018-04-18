import io.ktor.application.*
import io.ktor.http.*
import io.ktor.samples.guice.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.*

class GuiceApplicationTest {
    @Test
    fun testRoot(): Unit = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(
                """
                    |<!DOCTYPE html>
                    |<html>
                    |  <head>
                    |    <title>Ktor: guice</title>
                    |  </head>
                    |  <body>
                    |    <p>Hello from Ktor Guice sample application</p>
                    |    <p>Call Information: /</p>
                    |  </body>
                    |</html>
                    |
                """.trimMargin(),
                response.content
            )
        }
    }
}