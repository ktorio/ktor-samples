import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

private const val DEFAULT_REPO = "https://repo.maven.apache.org/maven2/io/ktor"

data class Artifact(
    val id: String, val versions: Set<String>
)

data class VersionsDiff(
    val addedArtifacts: List<Artifact>, val removedArtifacts: List<Artifact>
)

fun main(args: Array<String>) = runBlocking {
    val firstVersion = args[0]
    val secondVersion = args[1]

    val repo = if (args.size > 2) args[3] else DEFAULT_REPO

    println("Fetching diff for $repo $firstVersion -> $secondVersion")

    val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
        }
        install(HttpRequestRetry) {
            exponentialDelay()
            maxRetries = 5
        }
    }

    val names = client.downloadArtifactNames(repo)

    println("Downloading artifacts")

    val artifacts = names.map { name ->
        async {
            try {
                val versions = client.downloadArtifactVersions(repo, name)
                print("#")
                Artifact(name, versions)
            } catch (cause: Throwable) {
                cause.printStackTrace()
                throw cause
            }
        }
    }.awaitAll()

    println("Downloading done")

    val diff = diffVersions(artifacts, firstVersion, secondVersion)

    println("In upgrade $firstVersion -> $secondVersion added:")
    for (artifact in diff.addedArtifacts) {
        println("  ${artifact.id}")
    }

    println("In upgrade $firstVersion -> $secondVersion removed:")
    for (artifact in diff.removedArtifacts) {
        println("  ${artifact.id}")
    }
}