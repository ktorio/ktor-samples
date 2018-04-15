package io.ktor.samples.kweet

import io.ktor.http.*
import io.ktor.samples.kweet.dao.*
import io.ktor.samples.kweet.model.*
import io.ktor.server.testing.*
import io.mockk.*
import org.joda.time.*
import org.junit.Test
import kotlin.test.*

class KweetApplicationTest {
    val dao = mockk<DAOFacade>(relaxed = true)
    val date = DateTime.parse("2010-01-01T00:00+00:00")

    @Test
    fun testEmptyHome() = testApp {
        every { dao.top() } returns listOf()
        every { dao.latest() } returns listOf()

        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains("There are no kweets yet"))
        }
    }

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

    @Test
    fun testLoginFail() = testApp {
        this.handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "myuser", "password" to "invalid").formUrlEncode())
        }.apply {
            assertEquals(302, response.status()?.value)
            assertEquals("http://localhost/user", response.headers["Location"])
        }
    }

    @Test
    fun testLoginSuccess() = testApp {
        val password = "mylongpassword"
        val passwordHash = hash(password)
        val sessionCookieName = "SESSION"
        lateinit var sessionCookie: Cookie
        every { dao.user("test1", passwordHash) } returns User("test1", "test1@test.com", "test1", passwordHash)

        this.handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("userId" to "test1", "password" to password).formUrlEncode())
        }.apply {
            assertEquals(302, response.status()?.value)
            assertEquals("http://localhost/user/test1", response.headers["Location"])
            assertEquals(null, response.content)
            sessionCookie = response.cookies[sessionCookieName]!!
        }

        this.handleRequest(HttpMethod.Get, "/") {
            addHeader(HttpHeaders.Cookie, "$sessionCookieName=${encodeURLQueryComponent(sessionCookie.value)}")
        }.apply {
            assertTrue { response.content!!.contains("sign out") }
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ mainWithDependencies(dao) }) { callback() }
    }
}
