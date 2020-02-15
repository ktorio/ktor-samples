package io.ktor.samples.kodein

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for the [daggerApplication] module from DaggerApplication.
 */
class DaggerApplicationTest {

    @Test
    fun `get user`() = withTestApplication<Unit>(
        {
            kodeinApplication { daggerApplication(it) }
        }
    ) {
        handleRequest { method = HttpMethod.Get; uri = "/users/fake" }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                """
                <!DOCTYPE html>
                <html>
                  <body>
                    <h1>fake</h1>
                  </body>
                </html>
                """.trimIndent() + "\n",
                response.content
            )
        }
    }

    @Test
    fun `get default users`() = withTestApplication<Unit>(
        {
            kodeinApplication { daggerApplication(it) }
        }
    ) {
        handleRequest { method = HttpMethod.Get; uri = "/users/" }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
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
                response.content
            )
        }
    }

    // Note: a JVM bug prevents us from using `nice test names` when there's a local class defined in it.
    @Test
    fun testGetFakeUsers() = withTestApplication<Unit>(
        {
            class FakeRepository : Users.IRepository {
                override fun list() = listOf(Users.User("fake"))
            }
            kodeinApplication {
                daggerApplication(it)
                bind<Users.IRepository>(overrides = true) with singleton { FakeRepository() }
            }
        }
    ) {
        handleRequest { method = HttpMethod.Get; uri = "/users/" }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
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
                response.content
            )
        }
    }
}
