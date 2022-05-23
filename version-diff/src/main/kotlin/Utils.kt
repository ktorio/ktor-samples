import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*

private const val HREF_TOKEN = "href=\""

internal fun diffVersions(artifactInfo: List<Artifact>, from: String, to: String): VersionsDiff {
    val old = artifactInfo.filter { it.versions.contains(from) }
    val new = artifactInfo.filter { it.versions.contains(to) }

    val newArtifacts = new.filter { it !in old }
    val removedArtifacts = old.filter { it !in new }

    return VersionsDiff(newArtifacts, removedArtifacts)
}

internal suspend fun HttpClient.downloadArtifactVersions(repo: String, artifact: String): Set<String> =
    get("$repo/$artifact").bodyAsText().extractHrefContent().toSet()

internal suspend fun HttpClient.downloadArtifactNames(repo: String): List<String> =
    get(repo).bodyAsText().extractHrefContent()

internal fun String.extractHrefContent(): List<String> {
    var current = 0
    val result = mutableListOf<String>()
    while (true) {
        current = indexOf(HREF_TOKEN, current)
        if (current == -1) break

        current += HREF_TOKEN.length
        val end = indexOf("\"", current)

        val name = substring(current, end - 1)
        if (name != ".." && !name.startsWith("maven")) {
            result.add(name)
        }
    }

    return result
}
