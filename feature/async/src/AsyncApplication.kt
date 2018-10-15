package io.ktor.samples.async

import kotlinx.html.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.system.*

/**
 * A dedicated context for sample "compute-intensive" tasks.
 */
val compute = newFixedThreadPoolContext(4, "compute")

/**
 * An alias to simplify a suspending functional type.
 */
typealias DelayProvider = suspend (ms: Int) -> Unit

/**
 * The main entry point. We use @JvmOverloads so there is a signature with `Application.main()` that will be located
 * via reflection. This function is referenced by the application.conf file.
 *
 * We pass several arguments with services and providers so we can provide mocks when doing integration tests.
 *
 * For more information about this file: https://ktor.io/servers/configuration.html#hocon-file
 */
@JvmOverloads
fun Application.main(random: Random = Random(), delayProvider: DelayProvider = { delay(it.toLong()) }) {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    // Registers routes
    routing {
        // Tabbed browsers can wait for first request to complete in one tab before making a request in another tab.
        // Presumably they assume second request will hit 304 Not Modified and save on data transfer.
        // If you want to verify simultaneous connections, either use "curl" or use different URLs in different tabs
        // Like localhost:8080/1, localhost:8080/2, localhost:8080/3, etc
        get("/{...}") {
            val startTime = System.currentTimeMillis()
            call.respondHandlingLongCalculation(random, delayProvider, startTime)
        }
    }
}

/**
 * Function that will perform a long computation in a threadpool generating random numbers
 * and then will respond with the result.
 */
private suspend fun ApplicationCall.respondHandlingLongCalculation(random: Random, delayProvider: DelayProvider, startTime: Long) {
    val queueTime = System.currentTimeMillis() - startTime
    var number = 0
    val computeTime = measureTimeMillis {
        // We specify a coroutine context, that will use a thread pool for long computing operations.
        // In this case it is not necessary since we are "delaying", not sleeping the thread.
        // But serves as an example of what to do if we want to perform slow non-asynchronous operations
        // that would block threads.
        withContext(compute) {
            for (index in 0 until 300) {
                delayProvider(10)
                number += random.nextInt(100)
            }
        }
    }

    // Responds with an HTML file, generated with the kotlinx.html DSL.
    // More information about this DSL: https://github.com/Kotlin/kotlinx.html
    respondHtml {
        head {
            title { +"Ktor: async" }
        }
        body {
            p {
                +"Hello from Ktor Async sample application"
            }
            p {
                +"We calculated number $number in $computeTime ms of compute time, spending $queueTime ms in queue."
            }
        }
    }
}
