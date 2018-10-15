package io.ktor.samples.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import java.util.*

@Location("/manual")
class Manual

@Location("/userTable")
class SimpleUserTable

object BasicAuthApplication {
    @JvmStatic
    fun main(args: Array<String>): Unit {
        embeddedServer(Netty, port = 8080, module = Application::basicAuthApplication).start(wait = true)
    }
}

val hashedUserTable = UserHashedTableAuth(
    table = mapOf(
        "test" to Base64.getDecoder().decode("VltM4nfheqcJSyH887H+4NEOm2tDuKCl83p5axYXlF0=") // sha256 for "test"
    )
)

fun Application.basicAuthApplication() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(Authentication) {
        basic {
            realm = "ktor"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
        basic("hashed") {
            realm = "ktor"
            validate({ hashedUserTable.authenticate(it) })
        }
    }

    routing {
        authenticate("hashed") {
            get<SimpleUserTable> {
                call.respondText("Success, ${call.principal<UserIdPrincipal>()?.name}")
            }
        }

        authenticate {
            get<Manual> {
                call.respondText("Success, ${call.principal<UserIdPrincipal>()?.name}")
            }

            route("/admin/*") {
                get("ui") {
                    call.respondText("Success, ${call.principal<UserIdPrincipal>()?.name}")
                }
            }
        }

        // List available urls for testing
        get("/") {
            call.respondHtml {
                body {
                    ul {
                        for (item in listOf("/manual", "/userTable", "/admin/demo/ui")) {
                            li { a(href = item) { +item } }
                        }
                    }
                }
            }
        }
    }
}
