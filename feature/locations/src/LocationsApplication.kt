package io.ktor.samples.locations

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.html.*
import java.util.*

@Location("/") class index()
@Location("/number") class number(val value: Int)

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    routing {
        get<index> {
            call.respondHtml {
                head {
                    title { +"Ktor: locations" }
                }
                body {
                    p {
                        +"Hello from Ktor locations sample application"
                    }
                    h1 {
                        +"Choose a Number"
                    }
                    ul {
                        val rnd = Random()
                        (0..5).forEach {
                            li {
                                val number = number(rnd.nextInt(1000))
                                a(href = locations.href(number)) {
                                    +"Number #${number.value}"
                                }
                            }
                        }
                    }
                }
            }
        }

        get<number> { number ->
            call.respondHtml {
                head {
                    title { +"Ktor: locations" }
                }
                body {
                    h1 {
                        +"Number is ${number.value}"
                    }
                }
            }
        }
    }
}
