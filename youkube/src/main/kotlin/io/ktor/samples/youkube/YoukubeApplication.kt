package io.ktor.samples.youkube

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException
import java.util.*

/*
 * Typed routes using the [Resources] plugin: https://ktor.io/docs/type-safe-routing.html
 */

/**
 * A resource for a specific video stream by [id].
 */
@Resource("/video/{id}")
class VideoStream(val id: Long)

/**
 * A resource for a specific video page by [id].
 */
@Resource("/video/page/{id}")
class VideoPage(val id: Long)

/**
 * A resource for login a [userName] with a [password].
 */
@Resource("/login")
class Login(val userName: String = "", val password: String = "")

/**
 * A resource for uploading videos.
 */
@Resource("/upload")
class Upload()

/**
 * The index root page with a summary of the site.
 */
@Resource("/")
class Index()

/**
 * A session of this site, that just contains the [userId].
 */
data class YouKubeSession(val userId: String)

/**
 * An entry point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * For more information about this file: https://ktor.io/docs/configurations.html#configuration-file
 */
fun Application.main() {
    // This adds the Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses the logger to log every call (request/response)
    install(CallLogging)
    // Allows using classes annotated with @Resource to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Resources)
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)
    // Supports for the Range, Accept-Range, and Content-Range headers.
    install(PartialContent)
    // This plugin enables compression automatically when accepted by the client.
    install(Compression) {
        default()
        excludeContentType(ContentType.Video.Any)
    }

    // Obtains the YouKube config key from the application.conf file.
    // Inside that key, we then read several configuration properties
    // with the [session.cookie], the [key] or the [upload.dir]
    val youkubeConfig = environment.config.config("youkube")
    val sessionCookieConfig = youkubeConfig.config("session.cookie")
    val key: String = sessionCookieConfig.property("key").getString()
    val sessionKey = hex(key)

    // We create the folder and a [Database] in that folder for the configuration [upload.dir].
    val uploadDirPath: String = youkubeConfig.property("upload.dir").getString()
    val uploadDir = File(uploadDirPath)
    if (!uploadDir.mkdirs() && !uploadDir.exists()) {
        throw IOException("Failed to create directory ${uploadDir.absolutePath}")
    }
    val database = Database(uploadDir)

    // We have a single user for testing in the user table: user=root, password=root.
    // So for the login, you have to use those credentials since you cannot register new users in this sample.
    val users = UserHashedTableAuth(
        getDigestFunction("SHA-256") { "ktor${it.length}" },
        table = mapOf(
            "root" to Base64.getDecoder().decode("76pc9N9hspQqapj30kCaLJA14O/50ptCg50zCA1oxjA=") // sha256 for "root"
        )
    )

    // Configure the session to be represented by a [YouKubeSession],
    // using the SESSION cookie to store it, and transforming it to be authenticated with the [hashKey].
    // It is sent in a plain text, but since it is authenticated can't be modified without knowing the secret [hashKey].
    install(Sessions) {
        cookie<YouKubeSession>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(sessionKey))
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
