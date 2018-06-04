package io.ktor.samples.filelisting

import io.ktor.application.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.network.util.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.experimental.*
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
            route("/myfiles") {
                files(root)
                listing(root)
            }
        }
    }.start(wait = true)
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
    return withContext(ioCoroutineDispatcher) {
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
