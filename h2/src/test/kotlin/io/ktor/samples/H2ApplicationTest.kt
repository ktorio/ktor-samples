package io.ktor.samples

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class H2ApplicationTest {
    @Test
    fun testGetRootReturnsHtmlMessageBoard() = testApplication {
        application {
            module()
        }

        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])

        val content = response.bodyAsText()
        assertTrue(content.contains("<h1>Message Board</h1>"))
        assertTrue(content.contains("<h2>Post Message</h2>"))
        assertTrue(content.contains("<h2>Messages</h2>"))
        assertTrue(content.contains("action=\"/yell\""))
    }

    @Test
    fun testGetMessagesReturnsPlainText() = testApplication {
        application {
            module()
        }

        val response = client.get("/messages")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/plain; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        val content = response.bodyAsText()
        assertNotNull(content)
    }

    @Test
    fun testPostYellRedirectsToRoot() = testApplication {
        application {
            module()
        }

        val response = client.post("/yell") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("text=Test+message")
        }

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/", response.headers[HttpHeaders.Location])
    }

    @Test
    fun testPostAndRetrieveMessage() = testApplication {
        application {
            module()
        }

        val testMessage = "Integration test message"

        val status = client.post("/yell") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("text=$testMessage")
        }.status

        assertEquals(HttpStatusCode.Found, status)

        val messagesResponse = client.get("/messages")
        val messagesContent = messagesResponse.bodyAsText()
        assertTrue(messagesContent.contains(testMessage))

        val rootResponse = client.get("/")
        val rootContent = rootResponse.bodyAsText()
        assertTrue(rootContent.contains(testMessage))
    }
}
