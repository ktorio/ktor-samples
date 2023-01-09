import io.ktor.http.*
import io.ktor.samples.kweet.*
import io.ktor.samples.kweet.dao.*
import io.ktor.samples.kweet.model.*
import io.ktor.server.testing.*
import io.mockk.*
import org.joda.time.*
import org.junit.Test
import kotlin.test.*

/**
 * Integration tests for the module [mainWithDependencies].
 *
 * Uses [testApp] in test methods to simplify the testing.
 */
class KweetApplicationTestLegacy {
    /**
     * A [mockk] instance of the [DAOFacade] to used to verify and mock calls on the integration tests.
     */
    val dao = mockk<DAOFacade>(relaxed = true)

    /**
     * Specifies a fixed date for testing.
     */
    val date = DateTime.parse("2010-01-01T00:00+00:00")

    /**
     * Tests that the [Index] page calls the [DAOFacade.top] and [DAOFacade.latest] methods just once.
     * And that when no [Kweets] are available, it displays "There are no kweets yet" somewhere.
     */
    @Test
    fun testEmptyHome() = testApp {
        every { dao.top() } returns listOf()
        every { dao.latest() } returns listOf()

        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains("There are no kweets yet"))
        }

        verify(exactly = 1) { dao.top() }
        verify(exactly = 1) { dao.latest() }
    }

    /**
     * Tests that the [Index] page calls the [DAOFacade.top] and [DAOFacade.latest] methods just once.
     * And that when some Kweets are available there is a call to [DAOFacade.getKweet] per provided kweet id
     * (the final application will cache with [DAOFacadeCache]).
     * Ensures that it DOESN'T display "There are no kweets yet" when there are kweets available,
     * and that the user of the kweets is also displayed.
     */
    @Test
    fun testHomeWithSomeKweets() = testApp {
        every { dao.getKweet(1) } returns Kweet(1, "user1", "text1", date, null)
        every { dao.getKweet(2) } returns Kweet(2, "user2", "text2", date, null)
        every { dao.top() } returns listOf(1)
        every { dao.latest() } returns listOf(2)

        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertFalse(response.content!!.contains("There are no kweets yet"))
            assertTrue(response.content!!.contains("user1"))
            assertTrue(response.content!!.contains("user2"))
        }

        verify(exactly = 2) { dao.getKweet(any()) }
        verify(exactly = 1) { dao.top() }
        verify(exactly = 1) { dao.latest() }
    }

    /**
     * Verifies the behaviour of a login failure. That it should be a redirection to the /user page.
     */
    @Test
    fun testLoginFail() = testApp {
        handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "myuser", "password" to "invalid").formUrlEncode())
        }.apply {
            assertEquals(302, response.status()?.value)
        }
    }

    /**
     * Verifies a chain of requests verifying the [Login].
     * It mocks a get [DAOFacade.user] request, checks that posting valid credentials to the /login form
     * redirects to the user [UserPage] for that user, and reuses the returned cookie for a request
     * to the [UserPage] and verifies that with that cookie/session, there is a "sign out" text meaning that
     * the user is logged in.
     */
    @Test
    fun testLoginSuccess() = testApp {
        val password = "mylongpassword"
        val passwordHash = hash(password)
        val sessionCookieName = "SESSION"
        lateinit var sessionCookie: Cookie
        every { dao.user("test1", passwordHash) } returns User("test1", "test1@test.com", "test1", passwordHash)

        handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "test1", "password" to password).formUrlEncode())
        }.apply {
            assertEquals(302, response.status()?.value)
            assertEquals("/user/test1", response.headers["Location"])
            assertEquals(null, response.content)
            sessionCookie = response.cookies[sessionCookieName]!!
        }

        handleRequest(HttpMethod.Get, "/") {
            addHeader(HttpHeaders.Cookie, "$sessionCookieName=${sessionCookie.value.encodeURLParameter()}")
        }.apply {
            assertTrue { response.content!!.contains("sign out") }
        }
    }

    /**
     * A private method used to reduce boilerplate when testing the application.
     */
    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ mainWithDependencies(dao) }) { callback() }
    }
}
