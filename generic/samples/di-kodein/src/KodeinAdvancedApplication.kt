package io.ktor.samples.kodein

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.util.*

/**
 * Entry point of the embedded-server sample program:
 *
 * io.ktor.samples.kodein.KodeinAdvancedApplicationKt.main
 *
 * This would start and wait a web-server at port 8080 using Netty.
 *
 * Uses the included [kodeinApplication] function
 * to register a more complex application that will
 * automatically detect mapped [KodeinController] subtypes
 * and will register the declared routes.
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        kodeinApplication { application ->
            application.apply {
                // This adds automatically Date and Server headers to each response, and would allow you to configure
                // additional headers served to each response.
                install(DefaultHeaders)
            }

            bindSingleton { Users.Repository() }
            bindSingleton { Users.Controller(it) }
        }
    }.start(wait = true)
}

/**
 * Users Controller, Router and Model. Can move to several files and packages if required.
 */
object Users {
    /**
     * The Users controller. This controller handles the routes related to users.
     * It inherits [KodeinController] that offers some basic functionality.
     * It only requires a [kodein] instance.
     */
    class Controller(kodein: Kodein) : KodeinController(kodein) {
        /**
         * [Repository] instance provided by [Kodein]
         */
        val repository: Repository by instance()

        /**
         * Registers the routes related to [Users].
         */
        override fun Routing.registerRoutes() {
            /**
             * GET route for [Routes.Users] /users, it responds
             * with a HTML listing all the users in the repository.
             */
            get<Routes.Users> {
                call.respondHtml {
                    body {
                        ul {
                            for (user in repository.list()) {
                                li { a(Routes.User(user.name).href) { +user.name } }
                            }
                        }
                    }
                }
            }

            /**
             * GET route for [Routes.User] /users/{name}, it responds
             * with a HTML showing the provided user by [Routes.User.name].
             */
            get<Routes.User> { user ->
                call.respondHtml {
                    body {
                        h1 { +user.name }
                    }
                }
            }
        }
    }

    /**
     * Data class representing a [User] by its [name].
     */
    data class User(val name: String)

    /**
     * [Users.Repository] that will handle operations related to the users on the system.
     */
    class Repository {
        private val initialUsers = listOf(User("test"), User("demo"))
        private val usersByName = LinkedHashMap<String, User>(initialUsers.associateBy { it.name })

        /**
         * Lists the available [Users.User] in this repository.
         */
        fun list() = usersByName.values.toList()
    }

    /**
     * A class containing routes annotated with [Location] and implementing [TypedRoute].
     */
    object Routes {
        /**
         * Route for listing users.
         */
        @Location("/users")
        object Users : TypedRoute

        /**
         * Route for showing a specific user from its [name].
         */
        @Location("/users/{name}")
        data class User(val name: String) : TypedRoute
    }
}

// Extensions

/**
 * Registers a [kodeinApplication] that that will call [kodeinMapper] for mapping stuff.
 * The [kodeinMapper] is a lambda that is in charge of mapping all the required.
 *
 * After calling [kodeinMapper], this function will search
 * for registered subclasses of [KodeinController], and will call their [KodeinController.registerRoutes] methods.
 */
fun Application.kodeinApplication(
    kodeinMapper: Kodein.MainBuilder.(Application) -> Unit = {}
) {
    val application = this

    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    application.install(Locations)

    /**
     * Creates a [Kodein] instance, binding the [Application] instance.
     * Also calls the [kodeInMapper] to map the Controller dependencies.
     */
    val kodein = Kodein {
        bind<Application>() with instance(application)
        kodeinMapper(this, application)
    }

    /**
     * Detects all the registered [KodeinController] and registers its routes.
     */
    routing {
        for (bind in kodein.container.tree.bindings) {
            val bindClass = bind.key.type.jvmType as? Class<*>?
            if (bindClass != null && KodeinController::class.java.isAssignableFrom(bindClass)) {
                val res by kodein.Instance(bind.key.type)
                println("Registering '$res' routes...")
                (res as KodeinController).apply { registerRoutes() }
            }
        }
    }
}

/**
 * A [KodeinAware] base class for Controllers handling routes.
 * It allows to easily get dependencies, and offers some useful extensions like getting the [href] of a [TypedRoute].
 */
abstract class KodeinController(override val kodein: Kodein) : KodeinAware {
    /**
     * Injected dependency with the current [Application].
     */
    val application: Application by instance()

    /**
     * Shortcut to get the url of a [TypedRoute].
     */
    val TypedRoute.href get() = application.locations.href(this)

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    abstract fun Routing.registerRoutes()
}

/**
 * Shortcut for binding singletons to the same type.
 */
inline fun <reified T : Any> Kodein.MainBuilder.bindSingleton(crossinline callback: (Kodein) -> T) {
    bind<T>() with singleton { callback(this@singleton.kodein) }
}

/**
 * Interface used for identify typed routes annotated with [Location].
 */
interface TypedRoute
