import io.ktor.application.*
import io.ktor.http.*
import io.ktor.samples.jackson.*
import io.ktor.server.testing.*
import kotlin.test.*

class JacksonApplicationTest {
    @Test
    fun testRoutes() {
        withTestApplication(Application::main) {
            handleRequest(HttpMethod.Get, "/v1").apply {
                assertEquals(200, response.status()?.value)
                assertEquals(
                    """
                        |{
                        |  "name" : "root",
                        |  "items" : [ {
                        |    "key" : "A",
                        |    "value" : "Apache"
                        |  }, {
                        |    "key" : "B",
                        |    "value" : "Bing"
                        |  } ],
                        |  "date" : [ 2018, 4, 13 ]
                        |}
                    """.trimMargin(),
                    response.content
                )
            }

            handleRequest(HttpMethod.Get, "/v1/item/B").apply {
                assertEquals(200, response.status()?.value)
                assertEquals(
                    """
                        |{
                        |  "key" : "B",
                        |  "value" : "Bing"
                        |}
                    """.trimMargin(),
                    response.content
                )
            }

            handleRequest(HttpMethod.Get, "/v1/item/unexistant_key").apply {
                assertEquals(404, response.status()?.value)
            }
        }
    }
}