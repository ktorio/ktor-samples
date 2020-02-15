package io.ktor.samples.dagger

import dagger.*
import io.ktor.http.*
import io.ktor.server.testing.*
import javax.inject.*
import kotlin.test.*

/**
 * Integration tests for the [daggerApplication] module from DaggerApplication.
 */
class DaggerApplicationTest {

    @Test
    fun `get user`() = withTestApplication<Unit>(
        {
            configuration()
            daggerApplication()
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
            configuration()
            daggerApplication()
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
            configuration()
            daggerApplication(DaggerTestApplicationComponent::builder) { dagger ->
                dagger.usersRepository(object : Users.IRepository {
                    override fun list() = listOf(Users.User("fake"))
                })
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

@Singleton
@Component(
    modules = [
        Users.FrontendModule::class
        // Not included, so that we can replace with @BindsInstance on @Component.Builder.
        //Users.BackendModule::class
    ]
)
private interface TestApplicationComponent : ApplicationComponent {

    @Component.Builder
    interface Builder : ApplicationComponent.Builder {

        @BindsInstance
        fun usersRepository(repository: Users.IRepository): Builder

        override fun build(): TestApplicationComponent
    }
}
