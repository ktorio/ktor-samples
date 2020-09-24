import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*

suspend fun runClient(engine: HttpClientEngine) {
    val client = HttpClient(engine)
    val response = client.get<String>("https://httpbin.org/get")
    print(response)
}