import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import io.ktor.util.cio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.io.*
import java.io.*
import java.net.*

fun main(args: Array<String>) {
    runBlocking {
        val client = HttpClient(Apache) {
            followRedirects = true
        }
        client.getAsTempFile("http://127.0.0.1:8080/") { file ->
            println(file.readBytes().size)
        }
    }
}

data class HttpClientException(val response: HttpResponse) : IOException("HTTP Error ${response.status}")

suspend fun HttpClient.getAsTempFile(url: String, callback: suspend (file: File) -> Unit) {
    val file = getAsTempFile(url)
    try {
        callback(file)
    } finally {
        file.delete()
    }
}

suspend fun HttpClient.getAsTempFile(url: String): File {
    val file = File.createTempFile("ktor", "http-client")
    val call = call {
        url(URL(url))
        method = HttpMethod.Get
    }
    if (!call.response.status.isSuccess()) {
        throw HttpClientException(call.response)
    }
    call.response.content.copyAndClose(file.writeChannel())
    return file
}
