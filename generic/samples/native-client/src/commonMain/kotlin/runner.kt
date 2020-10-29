import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*

suspend fun runClient(engine: HttpClientEngine) {
    val client = HttpClient(engine)
    try {
        val response = client.get<String>("http://example.com")
        print(response)
    } finally {
        // To prevent IllegalStateException https://youtrack.jetbrains.com/issue/KTOR-1071
        client.close()
        engine.close()
    }
}