import io.ktor.client.engine.curl.Curl
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // To prevent non-zero exit status because of detected memory leaks
    Platform.isMemoryLeakCheckerActive = false

    runClient(Curl.create())
}