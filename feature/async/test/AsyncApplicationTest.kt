package io.ktor.samples.async.tests

import io.ktor.http.*
import io.ktor.samples.async.*
import io.ktor.server.testing.*
import java.util.*
import kotlin.test.*

/**
 * We test the [main] ktor application module.
 */
class AsyncApplicationTest {
    /**
     * We are going to check the application. Since we are delaying and using random numbers,
     * we are going to provide a custom delayer that does nothing but log delays and a fixed seed random
     * to produce always the same consistent results.
     *
     * This way we can test the application fast and with predictability.
     */
    @Test
    fun `checks main route`(): Unit {
        val randomWithFixedSeed = Random(0L)
        val delayLog = arrayListOf<String>()

        withTestApplication(
            moduleFunction = {
                main(
                    random = randomWithFixedSeed,
                    delayProvider = { delayLog += "Delay($it)" }
                )
            }
        ) {
            // For '/1' request
            handleRequest(HttpMethod.Get, "/1").apply {
                // Checks that the request was handled
                assertTrue(requestHandled)

                // Checks that the route returned a 200 OK
                assertEquals(HttpStatusCode.Companion.OK, response.status())

                (response.content ?: "").let { text ->
                    // Matches the content against a pattern, and check that the delay was called.:
                    val match =
                        Regex("We calculated number (\\d+) in (\\d+) ms of compute time, spending \\d+ ms in queue.")
                            .find(text) ?: throw AssertionError("Response body didn't contain the expected pattern")

                    // Read the computed number.
                    val computedNumber = match.groupValues[1].toInt()

                    // Read the compute time.
                    val computeTime = match.groupValues[2].toInt()

                    // We forced a Random seed to always compute the same value.
                    assertEquals(15206, computedNumber)

                    // Since we provided a custom [delayProvider] for tests that completes immediately,
                    // we can check that this happened immediately (modulo computation overhead).
                    // But definitely less time than the 3000 milliseconds.
                    assertTrue("computeTime($computeTime) < 1000") { computeTime < 1000 }

                    // But we ensure that the delay was called with the right delay, the right number of times.
                    assertEquals(300, delayLog.count())
                    assertEquals("Delay(10)", delayLog.first())
                }
            }
        }
    }
}
