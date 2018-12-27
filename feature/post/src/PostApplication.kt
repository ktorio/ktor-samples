package io.ktor.samples.post

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    routing  {
        get("/"){
            call.respondHtml {
                head {
                    title { +"Ktor: post" }
                }
                body {
                    p {
                        +"Hello from Ktor Post sample application"
                    }
                    p {
                        +"File upload"
                    }
                    form("/form", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                        acceptCharset = "utf-8"
                        p {
                            label { +"Text field: " }
                            textInput { name = "textField" }
                        }
                        p {
                            label { +"File field: " }
                            fileInput { name = "fileField" }
                        }
                        p {
                            submitInput { value = "send" }
                        }
                    }
                }
            }
        }

        post("/form") {
            val multipart = call.receiveMultipart()
            call.respondTextWriter {
                if (!call.request.isMultipart()) {
                    appendln("Not a multipart request")
                } else {
                    while (true) {
                        val part = multipart.readPart() ?: break
                        when (part) {
                            is PartData.FormItem ->
                                appendln("FormItem: ${part.name} = ${part.value}")
                            is PartData.FileItem ->
                                appendln("FileItem: ${part.name} -> ${part.originalFileName} of ${part.contentType}")
                        }
                        part.dispose()
                    }
                }
            }
        }
    }
}
