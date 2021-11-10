package io.ktor.samples.kodein

import io.ktor.http.*
import io.ktor.server.testing.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.util.*
import kotlin.test.*

/**
 * Integration tests for the [myKodeinApp] module from KodeinSimpleApplication.
 */
class KodeinSimpleApplicationTest {
    /**
     * Test that creates the application with a custom [Kodein]
     * that maps toa custom [Random] instance.
     *
     * This [Random] returns always a constant value '7'.
     * We then call the single defined entry point to verify
     * that when used the Random generator the result is predictable and constant.
     *
     * Additionally we test that the [Random.next] function has been called once.
     */
    @Test
    fun testProvideFakeRandom() {
        val log = arrayListOf<String>()
        withTestApplication({
            /** Calls the [myKodeinApp] module with a [Random] class that always return 7. */
            myKodeinApp(Kodein {
                bind<Random>() with singleton {
                    object : Random() {
                        override fun next(bits: Int): Int = 7.also { log += "Random.next" }
                    }
                }
            })
        }) {
            /**
             * Checks that the single route, returns a constant value '7' from the mock,
             * and that the [Random.next] has been called just once
             */
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals("Random number in 0..99: 7", response.content)
                assertEquals(listOf("Random.next"), log)
            }
        }
    }
}
