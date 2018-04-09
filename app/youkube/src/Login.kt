package io.ktor.samples.youkube

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*

fun Route.login(users: UserHashedTableAuth) {
    val myFormAuthentication = "myFormAuthentication"

    application.install(Authentication) {
        form(myFormAuthentication) {
            userParamName = Login::userName.name
            passwordParamName = Login::password.name
            challenge = FormAuthChallenge.Redirect { call, c -> call.url(Login(c?.name ?: "")) }
            validate { users.authenticate(it) }
        }
    }

    location<Login> {
        authenticate(myFormAuthentication) {
            method(HttpMethod.Post) {
                handle {
                    val principal = call.principal<UserIdPrincipal>()
                    call.sessions.set(YouKubeSession(principal!!.name))
                    call.respondRedirect(Index())
                }
            }
        }

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

