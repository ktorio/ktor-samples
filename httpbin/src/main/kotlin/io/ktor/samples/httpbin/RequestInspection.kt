package io.ktor.samples.httpbin

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.userAgent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.requestInspection() {
    get("/headers") {
        call.respond(HeadersResponse(call.request.headers.toSortedMap()))
    }.describe {
        tag("Request inspection")
        summary = "Return the incoming request's HTTP headers."
        responses {
            HttpStatusCode.OK {
                description = "The request’s headers."
                schema = schemaWithExamples<HeadersResponse>("HeadersResponse")
            }
        }
    }

    get("/ip") {
        call.respond(IpResponse(call.request.local.remoteAddress))
    }.describe {
        tag("Request inspection")
        summary = "Returns the requester's IP Address."
        responses {
            HttpStatusCode.OK {
                description = "The Requester’s IP Address."
                schema = schemaWithExamples<IpResponse>("IpResponse")
            }
        }
    }

    get("/user-agent") {
        call.respond(UserAgentResponse(call.request.userAgent() ?: ""))
    }.describe {
        tag("Request inspection")
        summary = "Return the incoming requests's User-Agent header."
        responses {
            HttpStatusCode.OK {
                description = "The request’s User-Agent header."
                schema = schemaWithExamples<UserAgentResponse>("UserAgentResponse")
            }
        }
    }
}