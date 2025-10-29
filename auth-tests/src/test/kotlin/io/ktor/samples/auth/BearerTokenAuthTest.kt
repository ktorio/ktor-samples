package io.ktor.samples.auth

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class BearerTokenAuthTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testBearerTokenAuthSuccess() = testApplication {
        application {
            main()
        }
        
        // First, get a token by logging in
        val loginResponse = client.post("/login-bearer") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"user1","password":"password1"}""")
        }
        
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        
        val tokenResponse = json.decodeFromString<TokenResponse>(loginResponse.bodyAsText())
        val token = tokenResponse.token
        
        // Now use the token to access protected endpoint
        val response = client.get("/bearer") {
            bearerAuth(token)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, user1! (authenticated with Bearer Token)", response.bodyAsText())
    }
    
    @Test
    fun testBearerTokenAuthInvalidToken() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/bearer") {
            bearerAuth("invalid_token")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testBearerTokenAuthMissingToken() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/bearer")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testBearerTokenAuthInvalidLoginCredentials() = testApplication {
        application {
            main()
        }
        
        val loginResponse = client.post("/login-bearer") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"user1","password":"wrongpassword"}""")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
    }
    
    @Test
    fun testBearerTokenAuthMultipleUsers() = testApplication {
        application {
            main()
        }
        
        // Login as user1
        val loginResponse1 = client.post("/login-bearer") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"user1","password":"password1"}""")
        }
        val token1 = json.decodeFromString<TokenResponse>(loginResponse1.bodyAsText()).token
        
        // Login as user2
        val loginResponse2 = client.post("/login-bearer") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"user2","password":"password2"}""")
        }
        val token2 = json.decodeFromString<TokenResponse>(loginResponse2.bodyAsText()).token
        
        // Verify each token authenticates as the correct user
        val response1 = client.get("/bearer") {
            bearerAuth(token1)
        }
        assertEquals("Hello, user1! (authenticated with Bearer Token)", response1.bodyAsText())
        
        val response2 = client.get("/bearer") {
            bearerAuth(token2)
        }
        assertEquals("Hello, user2! (authenticated with Bearer Token)", response2.bodyAsText())
    }
    
    @Test
    fun testBearerTokenAuthTokenFormat() = testApplication {
        application {
            main()
        }
        
        // Try to access with a token that doesn't follow the expected format
        val response = client.get("/bearer") {
            bearerAuth("sometoken")  // doesn't start with "token_"
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
