import io.ktor.http.Url
import io.ktor.samples.mpp.client.ApplicationApi
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main() = runBlocking {
    val result = suspendCoroutine<String> { continuation ->
        ApplicationApi().apply {
            address = Url("https://ktor.io/pages.txt") // The default URL doesn't support CORS
        }.about {
            continuation.resume(it)
        }
    }

    println("Result: $result")
}