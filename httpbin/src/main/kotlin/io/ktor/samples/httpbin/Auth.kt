package io.ktor.samples.httpbin

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.auth() {
    authenticate("basic") {
        get("/basic-auth/{user}/{password}") {
            val principal = call.principal<UserIdPrincipal>()

            call.respond(UserAuthResponse(
                authenticated = principal != null,
                user = principal?.name ?: "",
            ))
        }.describe {
            tag("Auth")
            summary = "Prompts the user for authorization using HTTP Basic Auth."
            responses {
                HttpStatusCode.OK {
                    description = "Successful authentication."
                    schema = schemaWithExamples<UserAuthResponse>("UserAuthResponse")
                }
                HttpStatusCode.Unauthorized {
                    description = "Unsuccessful authentication."
                }
            }
        }
    }

    authenticate("bearer") {
        get("/bearer") {
            val token = call.principal<String>()

            call.respond(BearerAuthResponse(
                authenticated = token != null,
                token = token ?: "",
            ))
        }.describe {
            tag("Auth")
            summary = "Prompts the user for authorization using bearer authentication."
            responses {
                HttpStatusCode.OK {
                    description = "Successful authentication."
                    schema = schemaWithExamples<BearerAuthResponse>("BearerAuthResponse")
                }
                HttpStatusCode.Unauthorized {
                    description = "Unsuccessful authentication."
                }
            }
        }
    }

    authenticate("digest") {
        get("/digest-auth/{user}/{password}") {
            val principal = call.principal<UserIdPrincipal>()

            call.respond(UserAuthResponse(
                authenticated = principal != null,
                user = principal?.name ?: "",
            ))
        }.describe {
            tag("Auth")
            summary = "Prompts the user for authorization using Digest Auth."
            responses {
                HttpStatusCode.OK {
                    description = "Successful authentication."
                    schema = schemaWithExamples<UserAuthResponse>("UserAuthResponse")
                }
                HttpStatusCode.Unauthorized {
                    description = "Unsuccessful authentication."
                }
            }
        }

        get("/digest-auth/{user}/{password}/{algorithm}") {
            val principal = call.principal<UserIdPrincipal>()

            call.respond(UserAuthResponse(
                authenticated = principal != null,
                user = principal?.name ?: "",
            ))
        }.describe {
            tag("Auth")
            summary = "Prompts the user for authorization using Digest Auth + Algorithm."
            responses {
                HttpStatusCode.OK {
                    description = "Successful authentication."
                    schema = schemaWithExamples<UserAuthResponse>("UserAuthResponse")
                }
                HttpStatusCode.Unauthorized {
                    description = "Unsuccessful authentication."
                }
            }
        }
    }

    authenticate("hidden-basic") {
        get("/hidden-basic-auth/{user}/{password}") {
            val principal = call.principal<UserIdPrincipal>()

            call.respond(UserAuthResponse(
                authenticated = principal != null,
                user = principal?.name ?: "",
            ))
        }.describe {
            tag("Auth")
            summary = "Prompts the user for authorization using HTTP Basic Auth."
            responses {
                HttpStatusCode.OK {
                    description = "Successful authentication."
                    schema = schemaWithExamples<UserAuthResponse>("UserAuthResponse")
                }
                HttpStatusCode.NotFound {
                    description = "Unsuccessful authentication."
                }
            }
        }
    }
}