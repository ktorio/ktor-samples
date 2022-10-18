import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.samples.kweet.*
import io.ktor.samples.kweet.dao.*
import io.ktor.samples.kweet.model.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.*
import org.joda.time.*
import org.junit.Test
import kotlin.test.*

/**
 * Integration tests for the [main] module.
 */
class KweetApplicationTest {
    /**
     * A [mockk] instance of the [DAOFacade] to used to verify and mock calls on the integration tests.
     */
    val dao = mockk<DAOFacade>(relaxed = true)

    /**
     * Specifies a fixed date for testing.
     */
    val date = DateTime.parse("2010-01-01T00:00+00:00")

    @Test
    fun testEmptyHome() = testApplication {
        setupApp()

        every { dao.top() } returns listOf()
        every { dao.latest() } returns listOf()

        client.get("/").apply {
            assertEquals(200, status.value)
            assertTrue(bodyAsText().contains("There are no kweets yet"))
        }

        verify(exactly = 1) { dao.top() }
        verify(exactly = 1) { dao.latest() }
    }

    @Test
    fun testHomeWithSomeKweets() = testApplication {
        setupApp()

        every { dao.getKweet(1) } returns Kweet(1, "user1", "text1", date, null)
        every { dao.getKweet(2) } returns Kweet(2, "user2", "text2", date, null)
        every { dao.top() } returns listOf(1)
        every { dao.latest() } returns listOf(2)

        client.get("/").apply {
            assertEquals(200, status.value)
            assertFalse(bodyAsText().contains("There are no kweets yet"))
            assertTrue(bodyAsText().contains("user1"))
            assertTrue(bodyAsText().contains("user2"))
        }

        verify(exactly = 2) { dao.getKweet(any()) }
        verify(exactly = 1) { dao.top() }
        verify(exactly = 1) { dao.latest() }
    }

    /**
     * Verifies the behaviour of a login failure. That it should be a redirection to the /user page.
     */
    @Test
    fun testLoginFail() = testApplication {
        setupApp()

        client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "myuser", "password" to "invalid").formUrlEncode())
        }.apply {
            assertEquals(302, status.value)
        }
    }

    @Test
    fun testLoginSuccess() = testApplication {
        setupApp()
        val client = createClient {
            install(HttpCookies)
        }

        val password = "mylongpassword"
        val passwordHash = hash(password)
        every { dao.user("test1", passwordHash) } returns User("test1", "test1@test.com", "test1", passwordHash)

        client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "test1", "password" to password).formUrlEncode())
        }.apply {
            assertEquals(302, status.value)
            assertEquals("/user/test1", headers["Location"])
        }

        client.get("/").apply {
            assertEquals(200, status.value)
            assertTrue(bodyAsText().contains("sign out"))
        }
    }

    private fun ApplicationTestBuilder.setupApp() {
        application {
            mainWithDependencies(dao)
        }
        environment {
            config = MapApplicationConfig()
        }
    }
}
