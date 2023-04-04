package io.ktor.samples.jwtauth

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Serializable
data class User(val username: String, val password: String)

// This method is called via reflection by the fully qualified name of the module in the application.conf file
@Suppress("UNUSED")
fun Application.main() {
    val issuer = environment.config.property("jwt.issuer").getString()
    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    main(jwkProvider)
}

fun Application.main(jwkProvider: JwkProvider) {
    install(ContentNegotiation) {
        json()
    }
    val privateKeyString = environment.config.property("jwt.privateKey").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    routing {
        post("/login") {
            val user = call.receive<User>()
            // Check username and password
            // ...

            val publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey
            val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
            call.respondText { token }
        }

        authenticate("auth-jwt") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                call.respondText("Hello, $username!")
            }
        }
        route(".well-known") {
            resource("jwks.json")
        }
    }
}
