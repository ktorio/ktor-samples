package io.ktor.samples.structuredlogging

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(true)
}

fun Application.module() {
    intercept(ApplicationCallPipeline.Infrastructure) {
        val requestId = UUID.randomUUID()
        log.attach("req.Id", requestId.toString(), {
            log.info("Interceptor[start]")
            proceed()
            log.info("Interceptor[end]")
        })
    }
    routing {
        get("/") {
            log.info("Respond[start]")
            call.respondText("HELLO WORLD")
            log.info("Respond[end]")
        }
    }
}
