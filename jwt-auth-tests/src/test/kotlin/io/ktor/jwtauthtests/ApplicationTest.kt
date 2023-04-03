package io.ktor.jwtauthtests

import com.auth0.jwk.Jwk
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun userIsGreetedProperly() = testApplication {
        environment {
            config = MapApplicationConfig(
                "jwt.privateKey" to "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAtfJaLrzXILUg1U3N1KV8yJr92GHn5OtYZR7qWk1Mc4cy4JGjklYup7weMjBD9f3bBVoIsiUVX6xNcYIr0Ie0AQIDAQABAkEAg+FBquToDeYcAWBe1EaLVyC45HG60zwfG1S4S3IB+y4INz1FHuZppDjBh09jptQNd+kSMlG1LkAc/3znKTPJ7QIhANpyB0OfTK44lpH4ScJmCxjZV52mIrQcmnS3QzkxWQCDAiEA1Tn7qyoh+0rOO/9vJHP8U/beo51SiQMw0880a1UaiisCIQDNwY46EbhGeiLJR1cidr+JHl86rRwPDsolmeEF5AdzRQIgK3KXL3d0WSoS//K6iOkBX3KMRzaFXNnDl0U/XyeGMuUCIHaXv+n+Brz5BDnRbWS+2vkgIe9bUNlkiArpjWvX+2we",
                "jwt.issuer" to "issuer.test",
                "jwt.audience" to "audience",
                "jwt.realm" to "realm",
            )
        }

        application {
            main {
                Jwk(
                    "6f8856ed-9189-488f-9011-0ff4b6c08edc",
                    "RSA",
                    "RS256",
                    "",
                    listOf(),
                    "",
                    listOf(),
                    "",
                    mapOf(
                        "n" to "tfJaLrzXILUg1U3N1KV8yJr92GHn5OtYZR7qWk1Mc4cy4JGjklYup7weMjBD9f3bBVoIsiUVX6xNcYIr0Ie0AQ",
                        "e" to "AQAB"
                    )
                )
            }
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
}