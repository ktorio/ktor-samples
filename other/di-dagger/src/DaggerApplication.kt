package io.ktor.samples.kodein

import dagger.*
import dagger.multibindings.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import javax.inject.*

/**
 * Entry point of the embedded-server sample program:
 *
 * io.ktor.samples.kodein.KodeinAdvancedApplicationKt.main
 *
 * This would start and wait a web-server at port 8080 using Netty.
 *
 * Uses the included [daggerApplication] function
 * to register a more complex application that will
 * automatically detect mapped [LocationBasedController] subtypes
 * and will register the declared routes.
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        daggerApplication()
    }.start(wait = true)
}

@Singleton
@Component(
    modules = [
        Users.FrontendModule::class,
        Users.BackendModule::class
        // other RouteGroup.FrontendModules and RouteGroup.BackendModules come here
    ]
)
interface ApplicationComponent {
    val controllers: ControllerRegistrar

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }
}

/**
 * Users Controller, Router and Model. Can move to several files and packages if required.
 */
object Users {

    /**
     * Published dependencies in this route group.
     */
    @Module
    interface FrontendModule {

        @Binds
        @IntoSet
        fun controller(impl: Controller): LocationBasedController
    }

    /**
     * Internal dependencies in this route group.
     */
    @Module
    interface BackendModule {

        @Binds
        fun repository(impl: Repository): IRepository
    }

    /**
     * The Users controller. This controller handles the routes related to users.
     * It inherits [LocationBasedController] that offers some basic functionality.
     */
    @Singleton
    class Controller @Inject constructor(
        application: Application,
        private val repository: IRepository
    ) : LocationBasedController(application) {

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
     * Repository that will handle operations related to the users on the system.
     */
    interface IRepository {
        fun list() : List<User>
    }

    /**
     * Fake in-memory implementation of [Users.IRepository] for demo purposes.
     */
    @Singleton
    class Repository @Inject constructor() : IRepository {
        private val initialUsers = listOf(User("test"), User("demo"))
        private val usersByName = initialUsers.associateBy { it.name }

        /**
         * Lists the available [Users.User] in this repository.
         */
        override fun list() = usersByName.values.toList()
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
 * for registered subclasses of [LocationBasedController], and will call their [LocationBasedController.registerRoutes] methods.
 */
fun Application.daggerApplication() = daggerApplication(DaggerApplicationComponent::builder)
fun <T : ApplicationComponent.Builder> Application.daggerApplication(
    createComponentBuilder: () -> T,
    initComponent: (T) -> Unit = { }
) {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // Allows to use classes annotated with @Location to represent URLs.
    // They are typed, can be constructed to generate URLs, and can be used to register routes.
    install(Locations)

    // Create Dagger Component Builder via generic method.
    val builder: T = createComponentBuilder()
    // Initialize mandatory application instance in Dagger graph.
    builder.application(this)
    // Initialize rest of the component externally.
    initComponent(builder)
    // Finish building the dagger graph.
    val dagger = builder.build()

    // Initialize the routes in the application
    dagger.controllers.apply { register() }
}

/**
 * Registers all [LocationBasedController] routes.
 */
class ControllerRegistrar @Inject constructor(
    private val controllers: Set<@JvmSuppressWildcards LocationBasedController>
) {
    fun Application.register() {
        routing {
            controllers.forEach { controller ->
                println("Registering '$controller' routes...")
                controller.apply { registerRoutes() }
            }
        }
    }
}

/**
 * Base class for Controllers handling routes.
 * It allows to easily get dependencies, and offers some useful extensions like getting the [href] of a [TypedRoute].
 */
abstract class LocationBasedController(private val application: Application) {

    /**
     * Shortcut to get the url of a [TypedRoute] based on [Location.path]
     */
    @KtorExperimentalLocationsAPI
    val TypedRoute.href
        get() = application.locations.href(this)

    /**
     * Method that subtypes must override to register the handled [Routing] routes.
     */
    abstract fun Routing.registerRoutes()
}

/**
 * Interface used for identify typed routes annotated with [Location].
 */
interface TypedRoute
