package io.ktor.samples.kodein

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        kodeinApplication(
            // Register controllers
            { Users.Controller(it) }
        ) { application ->
            application.install(Locations)
            bind<Users.Repository>() with singleton { Users.Repository() }
        }
    }.start(wait = true)
}

// Users Controller, Router and Model. Can move to several files and packages.

object Users {
    class Controller(kodein: Kodein) : KodeinController(kodein) {
        val repository: Repository by instance()

        override fun Routing.registerRoutes() {
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

            get<Routes.User> { user ->
                call.respondHtml {
                    body {
                        h1 { +user.name }
                    }
                }
            }
        }
    }

    data class User(val name: String)

    class Repository {
        private val initialUsers = listOf(User("test"), User("demo"))
        private val usersByName = LinkedHashMap<String, User>(initialUsers.associateBy { it.name })

        fun list() = usersByName.values.toList()
    }

    object Routes {
        @Location("/users")
        object Users : TypedRoute

        @Location("/users/{name}")
        data class User(val name: String) : TypedRoute
    }
}

// Extensions

fun Application.kodeinApplication(
    vararg controllerBuilders: Kodein.(Kodein) -> KodeinController,
    kodeinMapper: Kodein.MainBuilder.(Application) -> Unit = {}
) {
    val app = this
    val kodein = Kodein {
        bind<Application>() with singleton { app }
        kodeinMapper(this, app)
    }

    for (controllerBuilder in controllerBuilders) {
        routing {
            controllerBuilder(kodein, kodein).apply {
                registerRoutes()
            }
        }
    }
}

abstract class KodeinController(override val kodein: Kodein) : KodeinAware {
    val app: Application by instance()
    fun routing(callback: Routing.() -> Unit) = app.routing(callback)

    val TypedRoute.href get() = app.locations.href(this)

    abstract fun Routing.registerRoutes()
}

interface TypedRoute
