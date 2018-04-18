import io.ktor.application.*
import io.ktor.http.*
import io.ktor.samples.html.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.*

class HtmlWidgetApplicationTest {
    @Test
    fun testRoot(): Unit = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertEquals(
                """
                    |<!DOCTYPE html>
                    |<html>
                    |  <head>
                    |    <title>Ktor: html</title>
                    |  </head>
                    |  <body>
                    |    <p>Hello from Ktor html sample application</p>
                    |    <div>Widgets are just functions</div>
                    |  </body>
                    |</html>
                    |
                """.trimMargin(),
                response.content
            )
        }
    }
}