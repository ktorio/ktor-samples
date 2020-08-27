package io.ktor.samples.redirectwithexception

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import kotlin.reflect.*

/**
 * Main method of the application. It embeds a [Netty] server at port 8080,
 * and configures the application's [module].
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

/**
 * Typed session that will be used in this application.
 */
data class MySession(val user: String)

/**
 * Hardcoded SECRET hash key used to hash the passwords, and to authenticate the sessions.
 */
val secretHashKey = hex("6819b57a326945c1968f45236581")

/**
 * Main [module] of this application.
 */
fun Application.module() {
    // Intall the sessions feature with [MySession] class stored in the "MYSESSION_ID" cookie authenticated
    // to prevent manipulation, unless the [secretHashKey] has been leaked.
    install(Sessions) {
        cookie<MySession>("MYSESSION_ID") {
            transform(SessionTransportTransformerMessageAuthentication(secretHashKey))
        }
    }

    // The StatusPages features allow to catch unhandled exceptions.
    // We are going to use it to catch redirection exceptions and actually perform redirects
    // We are also going to use it to catch exceptions of sessions not found to redirect to our desired pages.
    install(StatusPages) {
        registerRedirections()
        registerSessionNotFoundRedirect<MySession>("/login")
    }

    // The routing feature allows to execute different code based on the request paths and http methods.
    routing {
        // For the '/' GET route, we are going to try to get a session (if not found it will redirect to the /login page)
        // And if successfully, we will show the user name, and will show a button for logging out.
        get("/") {
            val session = call.sessions.getOrThrow<MySession>()
            call.respondHtml {
                body {
                    p { +"Logged as ${session.user}" }
                    form(action = "/logout", method = FormMethod.post) {
                        input(InputType.submit) { value = "Logout" }
                    }
                }
            }
        }

        val EXPECTED_USER = "user"
        val EXPECTED_PASS = "pass"

        // In the case of /login, we are going to do different things for GET and POST requests
        route("/login") {
            // For GET requests, we generate a HTML form for submitting to POST:/login
            get {
                call.respondHtml {
                    body {
                        form(action = "/login", method = FormMethod.post) {
                            input(InputType.hidden, name = "user") { value = EXPECTED_USER }
                            input(InputType.hidden, name = "pass") { value = EXPECTED_PASS }
                            input(InputType.submit) { value = "Login" }
                        }
                    }
                }
            }
            // For POST requests, we get the parameters from the form, validate the user and password,
            // set the session on successfully or redirects to the GET:/login page in case of failure.
            post {
                val params = call.receive<Parameters>()
                if (params["user"] != EXPECTED_USER || params["pass"] != EXPECTED_PASS) {
                    // This breaks the flow so the function stops executing at this point
                    redirect("/login")
                }

                call.sessions.set(MySession(params["user"]!!))
                redirect("/")
            }
        }
        // POST:/logout clears the session and redirects to the '/' page.
        post("/logout") {
            call.sessions.clear<MySession>()
            redirect("/")
        }
    }
}

///////////////////////////
// Redirection Utilities
///////////////////////////

/**
 * Exception used to be captured by [StatusPages] to perform a redirect.
 */
class RedirectException(val path: String, val permanent: Boolean) : Exception()

/**
 * Global function that throws a [RedirectException], to be catched by the [StatusPages] feature to perform a redirect
 * to [path].
 */
fun redirect(path: String, permanent: Boolean = false): Nothing = throw RedirectException(path, permanent)

/**
 * Extension method for configuring [StatusPages] that encapsulates the functionality of catching
 * the [RedirectException] and actually performing a redirection.
 */
fun StatusPages.Configuration.registerRedirections() {
    exception<RedirectException> { cause ->
        call.respondRedirect(cause.path, cause.permanent)
    }
}

////////////////////////////////
// Session Not Found Utilities
////////////////////////////////

/**
 * Exception used to be captured by [StatusPages] (or catched) to notify that the session couldn't be found,
 * so the application can do things like redirect. It stores the session that couldn't be retrieved to be able
 * to have different behaviours.
 */
class SessionNotFoundException(val clazz: KClass<*>) : Exception()

/**
 * Convenience method to try to get a exception of type [T], or to throw a [SessionNotFoundException] to
 * handle it either by catching or by using the [StatusPages] feature.
 */
inline fun <reified T> CurrentSession.getOrThrow(): T =
    this.get<T>() ?: throw SessionNotFoundException(T::class)

/**
 * Extension method for configuring [StatusPages] that encapsulates the functionality of catching
 * the [SessionNotFoundException] to redirect to the [path] page in the case of the session [T].
 */
inline fun <reified T> StatusPages.Configuration.registerSessionNotFoundRedirect(path: String) {
    exception<SessionNotFoundException> { cause ->
        if (cause.clazz == T::class) call.respondRedirect(path)
    }
}
