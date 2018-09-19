package io.ktor.samples.guice

import com.google.inject.*
import com.google.inject.name.*
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.html.*

fun Application.main() {
    // Create main injector
    val injector = Guice.createInjector(MainModule(this), HelloModule())

    // Intercept application call and put child injector into attributes
    intercept(ApplicationCallPipeline.Features) {
        call.attributes.put(InjectorKey, injector.createChildInjector(CallModule(call)))
    }
}

// attribute key for storing injector in a call
val InjectorKey = AttributeKey<Injector>("injector")

// accessor for injector from a call
val ApplicationCall.injector: Injector get() = attributes[InjectorKey]

// A module for each call
class CallModule(private val call: ApplicationCall) : AbstractModule() {
    override fun configure() {
        bind(ApplicationCall::class.java).toInstance(call)
        bind(CallService::class.java)
    }
}

// A service bound asEagerSingleton so it installs routing
class HelloRoutes @Inject constructor(application: Application, @Named("hello-message") message: String) {
    init {
        application.routing {
            get("/") {
                call.respondHtml {
                    head {
                        title { +"Ktor: guice" }
                    }
                    body {
                        p {
                            +message
                        }
                        p {
                            +"Call Information: ${call.injector.getInstance(CallService::class.java).information()}"
                        }
                    }
                }
            }
        }
    }
}

// Some service providing data inside a call
class CallService @Inject constructor(private val call: ApplicationCall) {
    fun information() = call.request.uri
}

// Main module, binds application and routes
class MainModule(private val application: Application) : AbstractModule() {
    override fun configure() {
        bind(HelloRoutes::class.java).asEagerSingleton()
        bind(Application::class.java).toInstance(application)
    }
}

// Some other module providing named String binding, that is injected back in HelloRoutes
class HelloModule() : AbstractModule() {
    override fun configure() {}

    @Provides
    @Singleton
    @Named("hello-message")
    fun helloMessage(): String = "Hello from Ktor Guice sample application"
}
