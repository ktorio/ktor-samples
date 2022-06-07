package io.ktor.samples.plugins

import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.configureRouting() {
    val board = MessageBoard(this)

    routing {
        get("/") {
            val messages = board.list()

            call.respondHtml {
                head { title("Message Board") }

                body {
                    h1 { +"Message Board" }
                    h2 { +"Post Message" }
                    form {
                        action = "/yell"
                        method = FormMethod.post
                        textInput { name = "text"; placeholder = "Message text" }
                        submitInput { value = "Yell" }
                    }
                    h2 { +"Messages" }
                    ul {
                        messages.forEach {
                            li { +it.toString() }
                        }
                    }
                }
            }
        }

        get("/messages") {
            val messages = board.list()
            call.respondText(messages.joinToString())
        }
        post("/yell") {
            val text = call.receiveParameters()["text"] ?: error("Missing text parameter")
            board.post(text)
            call.respondRedirect("/")
        }
    }
}
