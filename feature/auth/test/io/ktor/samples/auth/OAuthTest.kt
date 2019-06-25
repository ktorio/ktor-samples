package io.ktor.samples.auth

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.*

class OAuthTest {
    @Test
    fun testOAuthLogin() {
        withTestApplication {
            lateinit var state: String

            val mockEngine = MockEngine { request ->
                when (request.url.fullUrl) {
                    "https://www.googleapis.com/oauth2/v3/token" -> {
                        val textContent = request.body as TextContent
                        assertEquals(ContentType.Application.FormUrlEncoded, textContent.contentType)
                        assertEquals(
                            "client_id=%2A%2A%2A.apps.googleusercontent.com&client_secret=%2A%2A%2A&grant_type=authorization_code&state=$state&code=mycode&redirect_uri=http%3A%2F%2F127.0.0.1%2Flogin%2Fgoogle",
                            textContent.text
                        )

                        respond("""{
                                    "access_token": "myaccesstoken",
                                    "token_type": "mytokentype",
                                    "expires_in": 3600,
                                    "refresh_token": "myrefreshtoken"
                                }""", headers = headersOf(
                            HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())
                        ))
                    }
                    else -> {
                        error("Unhandled ${request.url.fullUrl}")
                    }
                }
            }

            application.OAuthLoginApplicationWithDeps(
                oauthHttpClient = HttpClient(mockEngine)
            )

            handleRequest(HttpMethod.Get, "/login/google") {
                addHeader("Host", "127.0.0.1")
            }.let { call ->
                val location = call.response.headers["Location"] ?: ""
                assertEquals(
                    "https://accounts.google.com/o/oauth2/auth?client_id=%2A%2A%2A.apps.googleusercontent.com&redirect_uri=http%3A%2F%2F127.0.0.1%2Flogin%2Fgoogle&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fplus.login&state=****&response_type=code",
                    Regex("state=(\\w+)").replace(location, "state=****")
                )
                val stateInfo = Regex("state=(\\w+)").find(location)
                state = stateInfo!!.groupValues[1]
            }

            handleRequest(HttpMethod.Get, "/login/google?state=$state&code=mycode") {
                addHeader("Host", "127.0.0.1")
            }.let { call ->
                assertEquals("""
                    <!DOCTYPE html>
                    <html>
                      <head>
                        <title>Logged in</title>
                      </head>
                      <body>
                        <h1>You are logged in</h1>
                        <p>Your token is OAuth2(accessToken=myaccesstoken, tokenType=mytokentype, expiresIn=3600, refreshToken=myrefreshtoken, extraParameters=Parameters [access_token=[myaccesstoken], refresh_token=[myrefreshtoken], token_type=[mytokentype], expires_in=[3600]])</p>
                      </body>
                    </html>
                """.trimIndent(), call.response.content?.trim())
            }
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
