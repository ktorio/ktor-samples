package io.ktor.samples.ssl

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import java.io.*
import io.ktor.network.tls.certificates.*

fun main(args: Array<String>) {
    // generate SSL certificate
    val file = File("build/temporary.jks")
    if (!file.exists()) {
        file.parentFile.mkdirs()
        generateCertificate(file)
    }
    // run embedded server
    embeddedServer(Netty, commandLineEnvironment(args)).start()
}
