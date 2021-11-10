package io.ktor.samples.kodein

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.security.*
import java.util.*

/**
 * Entry point of the embedded-server program:
 *
 * io.ktor.samples.kodein.KodeinSimpleApplicationKt.main
 *
 * This would start and wait a web-server at port 8080 using Netty,
 * and would load the 'myKodeinApp' ktor module.
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        myKodeinApp()
    }.start(wait = true)
}

/**
 * Main and only module of the application.
 * This module creates a Kodein container ands sets
 * maps a Random to a singleton based on SecureRandom.
 * And then configures the application.
 */
fun Application.myKodeinApp() = myKodeinApp(Kodein {
    bind<Random>() with singleton { SecureRandom() }
})

/**
 * This is the application module that has a
 * preconfigured [kodein] instance as input.
 *
 * The idea of this method, is that the different modules
 * can call this with several configured kodein variants
 * and also you can call it from the tests setting mocks
 * instead of the default mappings.
 */
fun Application.myKodeinApp(kodein: Kodein) {
    val random by kodein.instance<Random>()

    routing {
        get("/") {
            val range = 0 until 100
            call.respondText("Random number in $range: ${random[range]}")
        }
    }
}

/**
 * Convenience [Random] extension operator method to get a random integral value inside the specified [range].
 */
private operator fun Random.get(range: IntRange) = range.first + this.nextInt(range.last - range.first)
