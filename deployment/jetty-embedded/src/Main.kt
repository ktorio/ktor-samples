package io.ktor.samples.hello

import io.ktor.server.engine.*
import io.ktor.server.jetty.*

fun main(args: Array<String>) {
    embeddedServer(Jetty, commandLineEnvironment(args)).start()
}
