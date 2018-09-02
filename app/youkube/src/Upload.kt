package io.ktor.samples.youkube

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.network.util.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.experimental.*
import kotlinx.html.*
import java.io.*

fun Route.upload(database: Database, uploadDir: File) {
    get<Upload> {
        val session = call.sessions.get<YouKubeSession>()
        if (session == null) {
            call.respondRedirect(Login())
        } else {
            call.respondDefaultHtml(emptyList(), CacheControl.Visibility.Private) {
                h2 { +"Upload video" }

                form(
                    call.url(Upload()),
                    classes = "pure-form-stacked",
                    encType = FormEncType.multipartFormData,
                    method = FormMethod.post
                ) {
                    acceptCharset = "utf-8"

                    label {
                        htmlFor = "title"; +"Title:"
                        textInput { name = "title"; id = "title" }
                    }

                    br()
                    fileInput { name = "file" }
                    br()

                    submitInput(classes = "pure-button pure-button-primary") { value = "Upload" }
                }
            }
        }
    }

    post<Upload> {
        val session = call.sessions.get<YouKubeSession>()
        if (session == null) {
            call.respond(HttpStatusCode.Forbidden.description("Not logged in"))
        } else {
            val multipart = call.receiveMultipart()
            var title = ""
            var videoFile: File? = null

            multipart.forEachPart { part ->
                if (part is PartData.FormItem) {
                    if (part.name == "title") {
                        title = part.value
                    }
                } else if (part is PartData.FileItem) {
                    val ext = File(part.originalFileName).extension
                    val file = File(
                        uploadDir,
                        "upload-${System.currentTimeMillis()}-${session.userId.hashCode()}-${title.hashCode()}.$ext"
                    )

                    part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }
                    videoFile = file
                }

                part.dispose()
            }

            val id = database.addVideo(title, session.userId, videoFile!!)

            call.respondRedirect(VideoPage(id))
        }
    }
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = ioCoroutineDispatcher
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}
