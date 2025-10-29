package io.ktor.samples.auth

import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionAuthTest {
    
    @Test
    fun testSessionAuthWithLogin() = testApplication {
        application {
            main()
        }
        
        val client = createClient {
            install(HttpCookies)
        }
        
        // First, login via form to establish a session
        val loginResponse = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1",
                    "password" to "password1"
                ).formUrlEncode()
            )
        }
        
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        
        // Now access the session-protected endpoint
        val sessionResponse = client.get("/session")
        
        assertEquals(HttpStatusCode.OK, sessionResponse.status)
        assertEquals("Hello, user1! (authenticated with Session)", sessionResponse.bodyAsText())
    }
    
    @Test
    fun testSessionAuthWithoutLogin() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/session")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testSessionLogout() = testApplication {
        application {
            main()
        }
        
        val client = createClient {
            install(HttpCookies)
        }
        
        // Login
        val loginResponse = client.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1",
                    "password" to "password1"
                ).formUrlEncode()
            )
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        
        // Verify session works
        val sessionResponse = client.get("/session")
        assertEquals(HttpStatusCode.OK, sessionResponse.status)
        
        // Logout
        val logoutResponse = client.post("/logout")
        assertEquals(HttpStatusCode.OK, logoutResponse.status)
        assertEquals("Logged out successfully", logoutResponse.bodyAsText())
        
        // Verify session no longer works
        val afterLogoutResponse = client.get("/session")
        assertEquals(HttpStatusCode.Unauthorized, afterLogoutResponse.status)
    }
    
    @Test
    fun testSessionAuthMultipleUsers() = testApplication {
        application {
            main()
        }
        
        // Create two separate clients with their own cookie storage
        val client1 = createClient {
            install(HttpCookies)
        }
        
        val client2 = createClient {
            install(HttpCookies)
        }
        
        // Login as user1
        client1.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user1",
                    "password" to "password1"
                ).formUrlEncode()
            )
        }
        
        // Login as user2
        client2.post("/login-form") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                listOf(
                    "username" to "user2",
                    "password" to "password2"
                ).formUrlEncode()
            )
        }
        
        // Verify each client has its own session
        val response1 = client1.get("/session").bodyAsText()
        assertTrue(response1.contains("user1"))
        
        val response2 = client2.get("/session").bodyAsText()
        assertTrue(response2.contains("user2"))
    }
}
