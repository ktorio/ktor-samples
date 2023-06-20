package io.ktorgraal

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import io.ktorgraal.plugins.configureRouting
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigureRoutingTest {

    @Test
    fun testGetHi() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals("Hello GraalVM!", bodyAsText())
        }
    }
}