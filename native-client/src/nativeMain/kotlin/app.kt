import io.ktor.client.engine.curl.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // To prevent 134 exit status https://youtrack.jetbrains.com/issue/KTOR-1220
    Platform.isMemoryLeakCheckerActive = false

    runClient(Curl.create())
}
