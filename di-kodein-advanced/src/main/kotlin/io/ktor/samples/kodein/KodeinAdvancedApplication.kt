package io.ktor.samples.kodein

import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.kodein.di.*
import org.kodein.type.jvmType

/**
 * Application entry point.
 *
 * Starts an embedded Netty server on port 8080
 * and bootstraps the application using the `kodeinApplication` function.
 *
 * Controllers are discovered automatically from DI bindings
 * and their routes are registered dynamically.
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        kodeinApplication { application ->
            advancedApplication(application)
        }
    }.start(wait = true)
}

/**
 * Configures application-specific DI bindings.
 */
internal fun DI.MainBuilder.advancedApplication(application: Application) {
    // Adds default HTTP headers (Date, Server, etc.)
    application.install(DefaultHeaders)

    // Bind repository and controller into DI container
    bind<Users.IRepository>() with singleton { Users.Repository() }
    bind<Users.Controller>() with singleton { Users.Controller(di) }
}

/**
 * Users domain: controller, repository and routing definitions.
 */
object Users {

    /**
     * Controller responsible for handling user-related routes.
     */
    class Controller(override val di: DI) : KodeinController() {

        // Repository instance injected from DI
        private val repository: IRepository by instance()

        /**
         * Registers routes handled by this controller.
         */
        override fun Routing.registerRoutes() {

            // GET /users
            get<Routes.Users> {
                call.respondHtml {
                    body {
                        ul {
                            for (user in repository.list()) {
                                li {
                                    a(application.href(Routes.User(user.name))) {
                                        +user.name
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // GET /users/{name}
            get<Routes.User> { resource ->
                call.respondHtml {
                    body {
                        h1 { +resource.name }
                    }
                }
            }
        }
    }

    /**
     * Domain model representing a user.
     */
    data class User(val name: String)

    /**
     * Repository contract.
     */
    interface IRepository {
        fun list(): List<User>
    }

    /**
     * Simple in-memory repository implementation.
     */
    class Repository : IRepository {
        private val initialUsers = listOf(User("test"), User("demo"))
        private val usersByName = initialUsers.associateBy { it.name }

        override fun list() = usersByName.values.toList()
    }

    /**
     * Type-safe route definitions.
     */
    object Routes {

        @Resource("/users")
        object Users

        @Resource("/users/{name}")
        data class User(val name: String)
    }
}

/**
 * Bootstraps DI and automatically registers
 * all discovered KodeinController subclasses.
 */
fun Application.kodeinApplication(
    kodeinMapper: DI.MainBuilder.(Application) -> Unit = {}
) {
    val application = this

    application.install(Resources)

    val kodein = DI {
        bind<Application>() with instance(application)
        kodeinMapper(this, application)
    }

    routing {

        fun findControllers(kodein: DI): List<KodeinController> =
            kodein.container.tree.bindings.keys
                .filter { bind ->
                    val clazz = bind.type.jvmType as? Class<*> ?: return@filter false
                    KodeinController::class.java.isAssignableFrom(clazz)
                }
                .map { bind ->
                    val result by kodein.Instance(bind.type)
                    result as KodeinController
                }

        findControllers(kodein).forEach { controller ->
            controller.apply { registerRoutes() }
        }
    }
}

/**
 * Base controller abstraction.
 */
abstract class KodeinController : DIAware {
    abstract fun Routing.registerRoutes()
}