package io.ktor.samples.jwtauth

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun userIsGreetedProperly() = testApplication {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val keyPair = generator.genKeyPair()

        environment {
            config = MapApplicationConfig(
                "jwt.privateKey" to Base64.getEncoder().encodeToString(keyPair.private.encoded),
                "jwt.issuer" to "issuer.test",
                "jwt.audience" to "audience",
                "jwt.realm" to "realm",
            )
        }

        application {
            main(fakeJwkProvider("6f8856ed-9189-488f-9011-0ff4b6c08edc", keyPair.public as RSAPublicKey))
        }

        val response =  client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                    {
                        "username": "jetbrains",
                        "password": "foobar"
                    }
                """.trimIndent()
            )
        }

        val token = response.bodyAsText()

        val greetings = client.get("/hello") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()

        assertEquals("Hello, jetbrains!", greetings)
    }

    @Suppress("SameParameterValue")
    private fun fakeJwkProvider(id: String, publicKey: RSAPublicKey): JwkProvider {
        return JwkProvider {
            Jwk(
                id,
                "RSA",
                "RS256",
                "",
                listOf(),
                "",
                listOf(),
                "",
                mapOf(
                    "n" to Base64.getUrlEncoder().encodeToString(publicKey.modulus.toByteArray()),
                    "e" to Base64.getUrlEncoder().encodeToString(publicKey.publicExponent.toByteArray())
                )
            )
        }
    }
}
