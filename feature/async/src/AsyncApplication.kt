package io.ktor.samples.async

import kotlinx.coroutines.experimental.*
import kotlinx.html.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import java.util.*
import kotlin.system.*

// a dedicated context for sample "compute-intensive" tasks
val compute = newFixedThreadPoolContext(4, "compute")

typealias DelayProvider = suspend (ms: Int) -> Unit

@JvmOverloads
fun Application.main(random: Random = Random(), delayProvider: DelayProvider = { delay(it) }) {
    install(DefaultHeaders)
    install(CallLogging)
    routing {
        // Tabbed browsers can wait for first request to complete in one tab before making a request in another tab.
        // Presumably they assume second request will hit 304 Not Modified and save on data transfer.
        // If you want to verify simultaneous connections, either use "curl" or use different URLs in different tabs
        // Like localhost:8080/1, localhost:8080/2, localhost:8080/3, etc
        get("/{...}") {
            val startTime = System.currentTimeMillis()
            withContext(compute) {
                call.handleLongCalculation(random, delayProvider, startTime)
            }
        }
    }
}

private suspend fun ApplicationCall.handleLongCalculation(random: Random, delayProvider: DelayProvider, startTime: Long) {
    val queueTime = System.currentTimeMillis() - startTime
    var number = 0
    val computeTime = measureTimeMillis {
        for (index in 0 until 300) {
            delayProvider(10)
            number += random.nextInt(100)
        }
    }
    
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
