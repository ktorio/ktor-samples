import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*

suspend fun runClient(engine: HttpClientEngine) {
    val client = HttpClient(engine)
    try {
        val response = client.get<String>("http://example.com")
        print(response)
    } finally {
        // To prevent "IllegalStateException: Cannot execute task because event loop was shut down"
        client.close()
        engine.close()
    }
}