package io.ktor.samples.kodein

import io.ktor.http.*
import io.ktor.server.testing.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.util.*
import kotlin.test.*

class KodeinSimpleApplicationTest {
    @Test
    fun testProvideFakeRandom() {
        withTestApplication({
            myKodeinApp(Kodein {
                bind<Random>() with provider {
                    object : Random() {
                        override fun next(bits: Int): Int = 7
                    }
                }
            })
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals("Random number in 0..99: 7", response.content)
            }
        }
    }
}
