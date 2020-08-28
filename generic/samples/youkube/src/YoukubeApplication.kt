@file:UseExperimental(KtorExperimentalLocationsAPI::class)

package io.ktor.samples.youkube

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import java.io.*
import java.util.*

/*
 * Typed routes using the [Locations] feature.
 */

/**
 * Location for a specific video stream by [id].
 */
@Location("/video/{id}")
data class VideoStream(val id: Long)

/**
 * Location for a specific video page by [id].
 */
@Location("/video/page/{id}")
data class VideoPage(val id: Long)

/**
 * Location for login a [userName] with a [password].
 */
@Location("/login")
data class Login(val userName: String = "", val password: String = "")

/**
 * Location for uploading videos.
 */
@Location("/upload")
class Upload()

/**
 * The index root page with a summary of the site.
 */
@Location("/")
class Index()

/**
 * Session of this site, that just contains the [userId].
 */
data class YouKubeSession(val userId: String)

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * For more information about this file: https://ktor.io/servers/configuration.html#hocon-file
 */
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)
    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Locations)
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)
    // Supports for Range, Accept-Range and Content-Range headers
    install(PartialContent)
    // This feature enables compression automatically when accepted by the client.
    install(Compression) {
        default()
        excludeContentType(ContentType.Video.Any)
    }

    // Obtains the youkube config key from the application.conf file.
    // Inside that key, we then read several configuration properties
    // with the [session.cookie], the [key] or the [upload.dir]
    val youkubeConfig = environment.config.config("youkube")
    val sessionCookieConfig = youkubeConfig.config("session.cookie")
    val key: String = sessionCookieConfig.property("key").getString()
    val sessionkey = hex(key)

    // We create the folder and a [Database] in that folder for the configuration [upload.dir].
    val uploadDirPath: String = youkubeConfig.property("upload.dir").getString()
    val uploadDir = File(uploadDirPath)
    if (!uploadDir.mkdirs() && !uploadDir.exists()) {
        throw IOException("Failed to create directory ${uploadDir.absolutePath}")
    }
    val database = Database(uploadDir)

    // We have a single user for testing in the user table: user=root, password=root
    // So for the login you have to use those credentials since you cannot register new users in this sample.
    val users = UserHashedTableAuth(
        getDigestFunction("SHA-256") { "ktor${it.length}" },
        table = mapOf(
            "root" to Base64.getDecoder().decode("76pc9N9hspQqapj30kCaLJA14O/50ptCg50zCA1oxjA=") // sha256 for "root"
        ))

    // Configure the session to be represented by a [YouKubeSession],
    // using the SESSION cookie to store it, and transforming it to be authenticated with the [hashKey].
    // it is sent in plain text, but since it is authenticated can't be modified without knowing the secret [hashKey].
    install(Sessions) {
        cookie<YouKubeSession>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(sessionkey))
        }
    }

    // Register all the routes available to this application.
    // To allow better scaling for large applications,
    // we have moved those route registrations into several extension methods and files.
    routing {
        login(users)
        upload(database, uploadDir)
        videos(database)
        styles()
    }
}

/**
 * Utility for performing non-permanent redirections using a typed [location] whose class is annotated with [Location].
 */
suspend fun ApplicationCall.respondRedirect(location: Any) = respondRedirect(url(location), permanent = false)
