package io.ktor.samples.filelisting

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.network.util.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.html.*
import java.io.*
import java.text.*
import java.util.*
import kotlin.Comparator

fun main(args: Array<String>) {
    val root = File("other/filelisting/files").takeIf { it.exists() }
            ?: File("files").takeIf { it.exists() }
            ?: error("Can't locate files folder")

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
                files(root)
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
                +"""
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
                "request.local" to request.local,
                "request.origin" to request.origin
            )) {
                h2 {
                    +name
                }
                table {
                    row("$name.version", value.version)
                    row("$name.method", value.method)
                    row("$name.scheme", value.scheme)
                    row("$name.host", value.host)
                    row("$name.port", value.port)
                    row("$name.remoteHost", value.remoteHost)
                    row("$name.uri", value.uri)
                }
            }

            for ((name, parameters) in listOf(
                "Query parameters" to request.queryParameters,
                "Headers" to request.headers
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
    val dir = staticRootFolder.combine(folder)
    val pathParameterName = "static-content-path-parameter"
    val dateFormat = SimpleDateFormat("dd-MMM-YYYY HH:mm")
    get("{$pathParameterName...}") {
        val relativePath = call.parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return@get
        val file = dir.combineSafe(relativePath)
        if (file.isDirectory) {
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
}

private fun File?.combine(file: File) = when {
    this == null -> file
    else -> resolve(file)
}

data class FileInfo(val name: String, val date: Date, val directory: Boolean, val size: Long)

suspend fun File.listSuspend(includeParent: Boolean = false): List<FileInfo> {
    val file = this
    return withContext(Dispatchers.IO) {
        listOfNotNull(if (includeParent) FileInfo("..", Date(), true, 0L) else null) + file.listFiles().toList().map {
            FileInfo(it.name, Date(it.lastModified()), it.isDirectory, it.length())
        }.sortedWith(comparators(
            Comparator { a, b -> -a.directory.compareTo(b.directory) },
            Comparator { a, b -> a.name.compareTo(b.name, ignoreCase = true) }
        ))
    }
}

fun <T> comparators(vararg comparators: Comparator<T>): Comparator<T> {
    return Comparator { l, r ->
        for (comparator in comparators) {
            val result = comparator.compare(l, r)
            if (result != 0) return@Comparator result
        }
        return@Comparator 0
    }
}

operator fun <T> Comparator<T>.plus(other: Comparator<T>): Comparator<T> = comparators(this, other)
