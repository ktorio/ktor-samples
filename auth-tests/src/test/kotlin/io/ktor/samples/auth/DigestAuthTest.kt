package io.ktor.samples.auth

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DigestAuthTest {
    
    @Test
    fun testDigestAuthChallenge() = testApplication {
        application {
            main()
        }
        
        // When accessing digest protected endpoint without credentials,
        // server should respond with 401 and WWW-Authenticate header
        val response = client.get("/digest")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        
        val wwwAuthenticate = response.headers[HttpHeaders.WWWAuthenticate]
        assertTrue(wwwAuthenticate != null, "WWW-Authenticate header should be present")
        assertTrue(wwwAuthenticate.contains("Digest"), "WWW-Authenticate should contain 'Digest'")
        assertTrue(wwwAuthenticate.contains("realm="), "WWW-Authenticate should contain realm")
        assertTrue(wwwAuthenticate.contains("nonce="), "WWW-Authenticate should contain nonce")
    }
    
    @Test
    fun testDigestAuthMissingCredentials() = testApplication {
        application {
            main()
        }
        
        val response = client.get("/digest")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
    
    @Test
    fun testDigestAuthInvalidCredentials() = testApplication {
        application {
            main()
        }
        
        // Get the challenge first
        val challengeResponse = client.get("/digest")
        val wwwAuthenticate = challengeResponse.headers[HttpHeaders.WWWAuthenticate]!!
        
        val realm = Regex("""realm="([^"]+)"""").find(wwwAuthenticate)?.groupValues?.get(1) ?: ""
        val nonce = Regex("""nonce="([^"]+)"""").find(wwwAuthenticate)?.groupValues?.get(1) ?: ""
        
        // Send request with invalid response hash
        val authResponse = client.get("/digest") {
            header(HttpHeaders.Authorization, 
                """Digest username="user1", realm="$realm", nonce="$nonce", uri="/digest", response="invalidhash"""")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, authResponse.status)
    }
}
