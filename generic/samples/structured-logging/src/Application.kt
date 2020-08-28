package io.ktor.samples.structuredlogging

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.*

/**
 * Main entrypoint of the application. Starts a Netty server on port 8080,
 * and applies the [module] that includes a custom logger supporting
 * structured logging by attaching named context objects to the call
 * and uses them when logging. It uses slf4j internally.
 *
 * After 0.9.5 once the CallId + CallLogging is included, this shouldn't be necessary and one could use MDC directly:
 * - Check: https://github.com/ktorio/ktor/pull/565
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(true)
}

/**
 * This [module] registers an interceptor to the infrastructure pipeline
 * that attaches a [UUID] representing the requestId to the [logger] logger of the call.
 *
 * Whenever a log is performed, all the attached context objects performed by [StructuredLogger.attach],
 * will be associated to that log message.
 */
fun Application.module() {
    intercept(ApplicationCallPipeline.Features) {
        val requestId = UUID.randomUUID()
        logger.attach("req.Id", requestId.toString()) {
            logger.info("Interceptor[start]")
            proceed()
            logger.info("Interceptor[end]")
        }
    }
    routing {
        get("/") {
            logger.info("Respond[start]")
            call.respondText("HELLO WORLD")
            logger.info("Respond[end]")
        }
    }
}
