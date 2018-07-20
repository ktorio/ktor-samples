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

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        myKodeinApp()
    }.start(wait = true)
}

fun Application.myKodeinApp() = myKodeinApp(Kodein {
    bind<Random>() with provider { SecureRandom() }
})

fun Application.myKodeinApp(kodein: Kodein) {
    val random by kodein.instance<Random>()

    routing {
        get("/") {
            val range = 0 until 100
            call.respondText("Random number in $range: ${random[range]}")
        }
    }
}

private operator fun Random.get(range: IntRange) = range.first + this.nextInt(range.last - range.first)
