import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

fun main() {
    runBlocking {
        val client = HttpClient {
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
    val file = withContext(Dispatchers.IO) {
        File.createTempFile("ktor", "http-client")
    }
    val response = request {
        url(url)
        method = HttpMethod.Get
    }
    if (!response.status.isSuccess()) {
        throw HttpClientException(response)
    }
    response.bodyAsChannel().copyAndClose(file.writeChannel())
    return file
}
