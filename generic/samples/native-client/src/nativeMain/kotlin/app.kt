import io.ktor.client.engine.curl.Curl
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main() = runBlocking {
    // To prevent non-zero exit status because of detected memory leaks
    Platform.isMemoryLeakCheckerActive = false

    runClient(Curl.create())
}