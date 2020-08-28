package io.ktor.samples.css

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(true)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "/styles.css", type = "text/css")
                }
                body {
                    styleCss {
                        rule("p.demo") {
                            color = Color.green
                        }
                    }
                    p {
                        +"Hello"
                        span {
                            style {
                                textDecoration(TextDecorationLine.underline)
                            }
                            +" World!"
                        }
                    }
                    p("myclass") {
                        +"I am using myclass"
                    }
                    p("demo") {
                        +"I'm a demo"
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}