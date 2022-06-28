import io.ktor.samples.mpp.client.*
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main() = runBlocking {
    val result = suspendCoroutine<String> { continuation ->
        ApplicationApi().about {
            continuation.resume(it)
        }
    }

    println("Result: $result")
}