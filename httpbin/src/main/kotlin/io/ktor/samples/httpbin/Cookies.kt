package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.util.getOrFail
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ExperimentalKtorApi
import kotlin.collections.toSortedMap

@OptIn(ExperimentalKtorApi::class)
fun Route.cookies() {
    get("/cookies") {
        call.respond(CookiesResponse(call.request.cookies.rawCookies.toSortedMap()))
    }.describe {
        tag("Cookies")
    }

    get("/cookies/delete") {
        call.response.headers.append(HttpHeaders.Location, "/cookies")
        call.response.headers.append(
            HttpHeaders.ContentType,
            ContentType.Text.Html.withCharset(Charsets.UTF_8).toString()
        )

        for (name in call.queryParameters.names()) {
            call.response.cookies.append(
                name = name,
                value = "",
                expires = GMTDate(0),
                maxAge = 0,
                path = "/",
            )
        }

        call.respond(HttpStatusCode.Found)
    }.describe {
        tag("Cookies")
    }

    get("/cookies/set") {
        call.response.headers.append(HttpHeaders.Location, "/cookies")
        call.response.headers.append(
            HttpHeaders.ContentType,
            ContentType.Text.Html.withCharset(Charsets.UTF_8).toString()
        )

        for (name in call.queryParameters.names()) {
            call.response.cookies.append(
                name = name,
                value = call.queryParameters[name] ?: "",
                path = "/",
            )
        }

        call.respond(HttpStatusCode.Found)
    }.describe {
        tag("Cookies")
    }

    get("/cookies/set/{name}/{value}") {
        call.response.headers.append(HttpHeaders.Location, "/cookies")
        call.response.headers.append(
            HttpHeaders.ContentType,
            ContentType.Text.Html.withCharset(Charsets.UTF_8).toString()
        )

        call.response.cookies.append(
            name = call.parameters.getOrFail("name"),
            value = call.parameters.getOrFail("value"),
            path = "/",
        )

        call.respond(HttpStatusCode.Found)
    }.describe {
        tag("Cookies")
    }
}