package io.ktor.samples.kweet

import io.ktor.samples.kweet.dao.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

/**
 * Register the index route of the website.
 */
fun Route.index(dao: DAOFacade) {
    // Uses the Location plugin to register a get route for '/'.
    get<Index> {
        // Tries to get the user from the session (null if failure)
        val user = call.sessions.get<KweetSession>()?.let { dao.user(it.userId) }

        // Obtains several lists of kweets using different sorting and filters.
        val top = dao.top(10).map { dao.getKweet(it) }
        val latest = dao.latest(10).map { dao.getKweet(it) }

        // Generates an ETag unique string for this route that will be used for caching.
        val etagString =
            user?.userId + "," + top.joinToString { it.id.toString() } + latest.joinToString { it.id.toString() }
        val etag = etagString.hashCode()

        // Uses FreeMarker to render the page.
        call.respond(
            FreeMarkerContent(
                "index.ftl",
                mapOf("top" to top, "latest" to latest, "user" to user),
                etag.toString()
            )
        )
    }
}
