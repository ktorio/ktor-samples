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

        this.handleRequest(HttpMethod.Get, "/").apply {
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

        this.handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(200, response.status()?.value)
            assertFalse(response.content!!.contains("There are no kweets yet"))
            assertTrue(response.content!!.contains("user1"))
            assertTrue(response.content!!.contains("user2"))
        }

        verify(exactly = 1) { dao.getKweet(1) }
        verify(exactly = 1) { dao.getKweet(2) }
        verify(exactly = 1) { dao.top() }
        verify(exactly = 1) { dao.latest() }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ mainWithDependencies(dao) }) { callback() }
    }
}