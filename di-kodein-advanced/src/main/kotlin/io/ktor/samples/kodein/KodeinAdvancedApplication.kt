package io.ktor.samples.kodein

import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import kotlinx.html.*
import org.kodein.di.*
import org.kodein.type.jvmType
import java.util.*

/**
 * An entry point of the embedded-server sample program:
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
fun main() {
    embeddedServer(Netty, port = 8080) {
        kodeinApplication { application ->
            // This adds Date and Server headers to each response and would allow you to configure
            // additional headers served to each response.
            application.install(DefaultHeaders)

            bindSingleton { Users.Repository() }
            bindSingleton { Users.Controller(it) }
        }
    }.start(wait = true)
}

/**
 * Users Controller, Router and Model. Can be moved to several files and packages if required.
 */
object Users {
    /**
     * The Users controller. This controller handles the routes related to users.
     * It inherits [KodeinController] that offers some basic functionality.
     * It only requires a [kodein] instance.
     */
    class Controller(override val di: DI) : KodeinController() {
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
             * with an HTML listing all the users in the repository.
             */
            get<Routes.Users> {
                call.respondHtml {
                    body {
                        ul {
                            for (user in repository.list()) {
                                li { a(application.href(Routes.User(user.name))) { +user.name } }
                            }
                        }
                    }
                }
            }

            /**
             * GET route for [Routes.User] /users/{name}, it responds
             * with an HTML showing the provided user by [Routes.User.name].
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
     * A data class representing a [User] by its [name].
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
     * A class containing routes annotated with [Resource] to implement type-safe routing.
     */
    object Routes {
        /**
         * Route for listing users.
         */
        @Resource("/users")
        object Users

        /**
         * Route for showing a specific user from its [name] using path parameter.
         */
        @Resource("/users/{name}")
        data class User(val name: String)
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
    kodeinMapper: DI.MainBuilder.(Application) -> Unit = {}
) {
    val application = this

    // Allows using classes annotated with @Resources to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    application.install(Resources)

    /**
     * Creates a [Kodein] instance, binding the [Application] instance.
     * Also calls the [kodeInMapper] to map the Controller dependencies.
     */
    val kodein = DI {
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
 * It allows to easily get dependencies, and offers some useful extensions.
 */
abstract class KodeinController : DIAware {
    /**
     * Injected dependency with the current [Application].
     */
    val application: Application by instance()

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    abstract fun Routing.registerRoutes()
}

/**
 * Shortcut for binding singletons to the same type.
 */
inline fun <reified T : Any> DI.MainBuilder.bindSingleton(crossinline callback: (DI) -> T) {
    bind<T>() with singleton { callback(this@singleton.di) }
}
