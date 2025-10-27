package io.ktor.samples.auth

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicAuthTest {
    
    @Test
    fun testBasicAuthSuccess() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/basic") {
            basicAuth("user1", "password1")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, user1! (authenticated with Basic Auth)", response.bodyAsText())
    }
    
    @Test
    fun testBasicAuthInvalidPassword() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/basic") {
            basicAuth("user1", "wrongpassword")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testBasicAuthInvalidUser() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/basic") {
            basicAuth("invaliduser", "password1")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testBasicAuthMissingCredentials() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/basic")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testBasicAuthMultipleUsers() = testApplication {
        application {
            main()
        }
        
        // Test user1
        val response1 = client.get("/basic") {
            basicAuth("user1", "password1")
        }
        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals("Hello, user1! (authenticated with Basic Auth)", response1.bodyAsText())
        
        // Test user2
        val response2 = client.get("/basic") {
            basicAuth("user2", "password2")
        }
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals("Hello, user2! (authenticated with Basic Auth)", response2.bodyAsText())
    }
}
