package io.ktor.samples.multiports

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val env = applicationEngineEnvironment {
        envConfig()
    }
    embeddedServer(Netty, env).start(true)
}

fun ApplicationEngineEnvironmentBuilder.envConfig() {
    module {
        main()
    }
    connector {
        host = "0.0.0.0"
        port = 9090
    }
    connector {
        host = "0.0.0.0"
        port = 8080
    }
}

fun Application.main() {
    routing {
        get("/") {
            if (call.request.local.port == 8080) {
                call.respondText("Connected to public API")
            } else {
                call.respondText("Connected to private API")
            }
        }
    }
}
