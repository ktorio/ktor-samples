package io.ktor.samples.timeout

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.delay

/**
 * This example demonstrates usage of HttpTimeout feature. It consists of two endpoints. First endpoint "/timeout"
 * emulates some long-running process that might hangup. Second endpoint "/proxy" represents a proxy to "/timeout" that
 * protects a user against such hang-ups. If user connects to "/proxy" and request hanged proxy will automatically abort
 * request using HttpTimeout feature.
 */
fun Application.timeoutApplication() {
    /* Client to perform proxy requests. */
    val client = HttpClient {
        install(HttpTimeout)
    }

    /* Target endpoint. */
    routing {
        get("/timeout") {
            delay(call.parameters["delay"]!!.toLong())
            call.respondText("It's OK!")
        }
    }

    /* Proxy endpoint. */
    routing {
        get("/proxy") {
            try {
                val response = client.get<String>(port = 8080, path = "/timeout") {
                    parameter("delay", call.parameters["delay"])

                    timeout {
                        requestTimeoutMillis = 1000
                    }
                }

                call.respondText(response)
            } catch (cause: Throwable) {
                call.respond(HttpStatusCode.GatewayTimeout, cause.message!!)
            }
        }
    }
}
