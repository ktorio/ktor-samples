package io.ktor.samples.auth

import com.auth0.jwk.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.concurrent.*

fun Application.jwkApplication() {
    val jwkUssuer = environment.config.property("jwt.domain").getString()
    val jwkAudience = environment.config.property("jwt.audience").getString()
    val jwkRealm = environment.config.property("jwt.realm").getString()
    val jwkProvider = makeJwkProvider(jwkUssuer)

    install(DefaultHeaders)
    install(CallLogging)
    install(Authentication) {
        jwt {
            verifier(jwkProvider, jwkUssuer)
            realm = jwkRealm
            validate { credential ->
                if (credential.payload.audience.contains(jwkAudience))
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }
    routing {
        authenticate {
            route("/who") {
                handle {
                    val principal = call.authentication.principal<JWTPrincipal>()
                    val subjectString = principal!!.payload.subject.removePrefix("auth0|")
                    call.respondText("Success, $subjectString")
                }
            }
        }
    }
}

private fun makeJwkProvider(issuer: String): JwkProvider = JwkProviderBuilder(issuer)
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build()

