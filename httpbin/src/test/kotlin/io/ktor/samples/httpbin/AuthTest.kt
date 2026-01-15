package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.DigestAuthCredentials
import io.ktor.client.plugins.auth.providers.digest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.auth.parseAuthorizationHeader
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class AuthTest {
    @Test
    fun basicAuthUserPassword() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/basic-auth")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun basicAuthNoCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/basic-auth/user/passwd")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(
            "Basic realm=\"Fake Realm\"",
            response.headers[HttpHeaders.WWWAuthenticate]
        )
    }

    @Test
    fun basicAuthInvalidCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/basic-auth/user/passwd") {
            val creds = Base64.encode("user:invalid".toByteArray())
            header(HttpHeaders.Authorization, "Basic $creds")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(
            "Basic realm=\"Fake Realm\"",
            response.headers[HttpHeaders.WWWAuthenticate]
        )
    }

    @Test
    fun basicAuthSuccess() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/basic-auth/user/passwd") {
            val creds = Base64.encode("user:passwd".toByteArray())
            header(HttpHeaders.Authorization, "Basic $creds")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()

        assertEquals(true, body.get("authenticated").asBoolean)
        assertEquals("user", body.get("user").asString)
    }

    @Test
    fun bearerAuthNoCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/bearer")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("Bearer", response.headers[HttpHeaders.WWWAuthenticate])
    }

    @Test
    fun bearerAuthNoToken() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/bearer") {
            header(HttpHeaders.Authorization, "Bearer")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("Bearer", response.headers[HttpHeaders.WWWAuthenticate])
    }

    @Test
    fun bearerAuthWithToken() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/bearer") {
            header(HttpHeaders.Authorization, "Bearer token")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<JsonObject>()

        assertEquals(true, body.get("authenticated").asBoolean)
        assertEquals("token", body.get("token").asString)
    }

    @Test
    fun digestAuthNoCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/digest-auth/user/passwd")
        assertEquals(HttpStatusCode.Unauthorized, response.status)

        val authHeader = response.headers[HttpHeaders.WWWAuthenticate]
        assertNotNull(authHeader)

        val parsedHeader = parseAuthorizationHeader(authHeader)
        assertNotNull(parsedHeader)
        assertIs<HttpAuthHeader.Parameterized>(parsedHeader)

        assertEquals("Digest", parsedHeader.authScheme)
        assertEquals("Fake Realm", parsedHeader.parameter("realm"))
        assertNotNull(parsedHeader.parameter("nonce"))
        assertEquals("MD5", parsedHeader.parameter("algorithm"))
    }

    @Test
    fun digestAuthValidCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
            install(Auth) {
                digest {
                    realm = "Fake Realm"
                    credentials {
                        DigestAuthCredentials("user", "passwd")
                    }
                }
            }
        }

        val response = client.get("/digest-auth/user/passwd")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<JsonObject>()

        assertEquals(true, body.get("authenticated").asBoolean)
        assertEquals("user", body.get("user").asString)
    }

    @Test
    fun digestAuthWithAlgorithmNoCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/digest-auth/user/passwd/SHA-256")
        assertEquals(HttpStatusCode.Unauthorized, response.status)

        val authHeader = response.headers[HttpHeaders.WWWAuthenticate]
        assertNotNull(authHeader)

        val parsedHeader = parseAuthorizationHeader(authHeader)
        assertNotNull(parsedHeader)
        assertIs<HttpAuthHeader.Parameterized>(parsedHeader)

        assertEquals("Digest", parsedHeader.authScheme)
        assertEquals("SHA-256", parsedHeader.parameter("algorithm"))
    }

    @Test
    fun digestAuthWithAlgorithmValidCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
            install(Auth) {
                digest {
                    realm = "Fake Realm"
                    algorithmName = "SHA-256"
                    credentials {
                        DigestAuthCredentials("user", "passwd")
                    }
                }
            }
        }

        val response = client.get("/digest-auth/user/passwd/SHA-256")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<JsonObject>()

        assertEquals(true, body.get("authenticated").asBoolean)
        assertEquals("user", body.get("user").asString)
    }

    @Test
    fun hiddenBasicAuthNoCredentials() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/hidden-basic-auth/user/passwd")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun hiddenBasicAuthSuccess() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/hidden-basic-auth/user/passwd") {
            val creds = Base64.encode("user:passwd".toByteArray())
            header(HttpHeaders.Authorization, "Basic $creds")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<JsonObject>()

        assertEquals(true, body.get("authenticated").asBoolean)
        assertEquals("user", body.get("user").asString)
    }
}