package io.ktor.samples.kodein

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.kodein.di.bind
import org.kodein.di.singleton
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * Integration tests for the [advancedApplication] module from KodeinAdvancedApplication.
 */
class KodeinAdvancedApplicationTest {

    @Test
    fun `get user`() =  testApplication {
        application {
            kodeinApplication { application ->
                advancedApplication(application)
            }
        }
        
        val response = client.get("/users/fake")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """
                <!DOCTYPE html>
                <html>
                  <body>
                    <h1>fake</h1>
                  </body>
                </html>
            """.trimIndent() + "\n",
            response.bodyAsText()
        )
    }

    @Test
    fun `get default users`() = testApplication {
        application {
            kodeinApplication { application ->
                advancedApplication(application)
            }
        }
        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """
                <!DOCTYPE html>
                <html>
                  <body>
                    <ul>
                      <li><a href="/users/test">test</a></li>
                      <li><a href="/users/demo">demo</a></li>
                    </ul>
                  </body>
                </html>
            """.trimIndent() + "\n",
            response.bodyAsText()
        )
    }

    // Note: a JVM bug (https://youtrack.jetbrains.com/issue/KT-25337)
    // prevents us from using `nice test names` when there's a local class defined in it.
    @Test
    fun testGetFakeUsers() = testApplication {
        application {
            class FakeRepository : Users.IRepository {
                override fun list() = listOf(Users.User("fake"))
            }
            kodeinApplication {
                advancedApplication(it)
                bind<Users.IRepository>(overrides = true) with singleton { FakeRepository() }
            }
        }

        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            """
                <!DOCTYPE html>
                <html>
                  <body>
                    <ul>
                      <li><a href="/users/fake">fake</a></li>
                    </ul>
                  </body>
                </html>
            """.trimIndent() + "\n",
            response.bodyAsText()
        )
    }
}
