import io.ktor.client.engine.cio.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking

@KtorExperimentalAPI
fun main() = runBlocking {
    runClient(CIO.create())
}