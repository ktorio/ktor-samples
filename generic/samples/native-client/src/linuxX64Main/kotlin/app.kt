import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main() = runBlocking {
    val client = HttpClient(CIO)
    val response = client.get<String>("https://httpbin.org/headers")
    print(response)
}