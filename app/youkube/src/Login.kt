package io.ktor.samples.youkube

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*

/**
 * Register [Login] related routes and features.
 */
fun Route.login(users: UserHashedTableAuth) {
    val myFormAuthentication = "myFormAuthentication"

    /**
     * Installs the Authentication feature that handles the challenge and parsing and attaches a [UserIdPrincipal]
     * to the [ApplicationCall] if the authentication succeedes.
     */
    application.install(Authentication) {
        form(myFormAuthentication) {
            userParamName = Login::userName.name
            passwordParamName = Login::password.name
            challenge { call.respondRedirect(call.url(Login(it?.name ?: ""))) }
            validate { users.authenticate(it) }
        }
    }

    /**
     * For the [Login] route:
     */
    location<Login> {
        /**
         * We have an authenticated POST handler, that would set a session when the [UserIdPrincipal] is set,
         * and would redirect to the [Index] page.
         */
        authenticate(myFormAuthentication) {
            post {
                val principal = call.principal<UserIdPrincipal>()
                call.sessions.set(YouKubeSession(principal!!.name))
                call.respondRedirect(Index())
            }
        }

        /**
         * For a GET method, we respond with an HTML with a form asking for the user credentials.
         */
        method(HttpMethod.Get) {
            handle<Login> {
                call.respondDefaultHtml(emptyList(), CacheControl.Visibility.Public) {
                    h2 { +"Login" }
                    form(call.url(Login()) { parameters.clear() }, classes = "pure-form-stacked", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                        acceptCharset = "utf-8"

                        label {
                            +"Username: "
                            textInput {
                                name = Login::userName.name
                                value = it.userName
                            }
                        }
                        label {
                            +"Password: "
                            passwordInput {
                                name = Login::password.name
                            }
                        }
                        submitInput(classes = "pure-button pure-button-primary") {
                            value = "Login"
                        }
                    }
                }
            }
        }
    }
}

