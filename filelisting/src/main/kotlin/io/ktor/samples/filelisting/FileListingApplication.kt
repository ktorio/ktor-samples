package io.ktor.samples.filelisting

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val root = File("samples/filelisting/files").takeIf { it.exists() }
        ?: File("files").takeIf { it.exists() }
        ?: error("Can't locate the 'files' folder")

    embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        routing {
            get("/") {
                call.respondRedirect("/myfiles")
            }
            get("/info") {
                call.respondInfo()
            }
            route("/myfiles") {
                staticFiles("", root)
                listing(root)
            }
        }
    }.start(wait = true)
}

suspend fun ApplicationCall.respondInfo() {
    fun TABLE.row(key: String, value: Any?) {
        tr {
            th { +key }
            td { +value.toString() }
        }
    }

    respondHtml {
        body {
            style {
                unsafe {
                    """
                    table {
                        font: 1em Arial;
                        border: 1px solid black;
                        width: 100%;
                    }
                    th {
                        background-color: #ccc;
                        width: 200px;
                    }
                    td {
                        background-color: #eee;
                    }
                    th, td {
                        text-align: left;
                        padding: 0.5em 1em;
                    }
                """.trimIndent()
                }
            }
            h1 {
                +"Ktor info"
            }
            h2 {
                +"Info"
            }
            table {
                row("request.httpVersion", request.httpVersion)
                row("request.httpMethod", request.httpMethod)
                row("request.uri", request.uri)
                row("request.path()", request.path())
                row("request.host()", request.host())
                row("request.document()", request.document())
                row("request.location()", request.location())
                row("request.queryParameters", request.queryParameters.formUrlEncode())

                row("request.userAgent()", request.userAgent())

                row("request.accept()", request.accept())
                row("request.acceptCharset()", request.acceptCharset())
                row("request.acceptCharsetItems()", request.acceptCharsetItems())
                row("request.acceptEncoding()", request.acceptEncoding())
                row("request.acceptEncodingItems()", request.acceptEncodingItems())
                row("request.acceptLanguage()", request.acceptLanguage())
                row("request.acceptLanguageItems()", request.acceptLanguageItems())

                row("request.authorization()", request.authorization())
                row("request.cacheControl()", request.cacheControl())

                row("request.contentType()", request.contentType())
                row("request.contentCharset()", request.contentCharset())
                row("request.isChunked()", request.isChunked())
                row("request.isMultipart()", request.isMultipart())

                row("request.ranges()", request.ranges())
            }

            for ((name, value) in listOf(
                "request.local" to request.local, "request.origin" to request.origin
            )) {
                h2 {
                    +name
                }
                table {
                    row("$name.version", value.version)
                    row("$name.method", value.method)
                    row("$name.scheme", value.scheme)
                    row("$name.host", value.localHost)
                    row("$name.port", value.localPort)
                    row("$name.remoteHost", value.remoteHost)
                    row("$name.uri", value.uri)
                }
            }

            for ((name, parameters) in listOf(
                "Query parameters" to request.queryParameters, "Headers" to request.headers
            )) {
                h2 {
                    +name
                }
                if (parameters.isEmpty()) {
                    +"empty"
                } else {
                    table {
                        for ((key, value) in parameters.flattenEntries()) {
                            row(key, value)
                        }
                    }
                }
            }

            h2 {
                +"Cookies"
            }
            table {
                for ((key, value) in request.cookies.rawCookies) {
                    row(key, value)
                }
            }
        }
    }
}

fun Route.listing(folder: File) {
    val pathParameterName = "static-content-path-parameter"
    val dateFormat = SimpleDateFormat("dd-MMM-YYYY HH:mm")
    get("{$pathParameterName...}") {
        val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
        val file = folder.combineSafe(relativePath)
        if (!file.isDirectory) return@get

        val isRoot = relativePath.trim('/').isEmpty()
        val files = file.listSuspend(includeParent = !isRoot)
        val base = call.request.path().trimEnd('/')
        call.respondHtml {
            body {
                h1 {
                    +"Index of $base/"
                }
                hr {}
                table {
                    style = "width: 100%;"
                    thead {
                        tr {
                            for (column in listOf("Name", "Last Modified", "Size", "MimeType")) {
                                th {
                                    style = "width: 25%; text-align: left;"
                                    +column
                                }
                            }
                        }
                    }
                    tbody {
                        for (finfo in files) {
                            val rname = if (finfo.directory) "${finfo.name}/" else finfo.name
                            tr {
                                td {
                                    if (finfo.name == "..") {
                                        a(File(base).parent) { +rname }
                                    } else {
                                        a("$base/$rname") { +rname }
                                    }
                                }
                                td {
                                    +dateFormat.format(finfo.date)
                                }
                                td {
                                    +(if (finfo.directory) "-" else "${finfo.size}")
                                }
                                td {
                                    +(ContentType.fromFilePath(finfo.name).firstOrNull()?.toString() ?: "-")
                                }
                            }
                        }
                    }
                }
                hr {}
            }
        }
    }
}

data class FileInfo(val name: String, val date: Date, val directory: Boolean, val size: Long)

suspend fun File.listSuspend(includeParent: Boolean = false): List<FileInfo> = withContext(Dispatchers.IO) {
    val parentEntry = if (includeParent) listOf(FileInfo("..", Date(), true, 0L)) else emptyList()
    val fileEntries = listFiles()?.map {
        FileInfo(it.name, Date(it.lastModified()), it.isDirectory, it.length())
    } ?: emptyList()

    (parentEntry + fileEntries)
        .sortedWith(compareBy<FileInfo> { !it.directory }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name })
}
