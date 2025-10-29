package io.ktor.samples.auth

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class FormAuthTest {
    
    @Test
    fun testFormAuthSuccess() = testApplication {
        application {
            main()
        }
        
        val response = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1",
                    "password" to "password1"
                ).formUrlEncode()
            )
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Login successful for user1", response.bodyAsText())
    }
    
    @Test
    fun testFormAuthInvalidPassword() = testApplication {
        application {
            main()
        }
        
        val response = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1",
                    "password" to "wrongpassword"
                ).formUrlEncode()
            )
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testFormAuthInvalidUser() = testApplication {
        application {
            main()
        }
        
        val response = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "invaliduser",
                    "password" to "password1"
                ).formUrlEncode()
            )
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testFormAuthMissingParameters() = testApplication {
        application {
            main()
        }
        
        val response = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1"
                ).formUrlEncode()
            )
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testFormAuthWithDifferentUsers() = testApplication {
        application {
            main()
        }
        
        // Test user1
        val response1 = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1",
                    "password" to "password1"
                ).formUrlEncode()
            )
        }
        assertEquals(HttpStatusCode.OK, response1.status)
        
        // Test user2
        val response2 = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "testuser",
                    "password" to "testpass"
                ).formUrlEncode()
            )
        }
        assertEquals(HttpStatusCode.OK, response2.status)
    }
}
