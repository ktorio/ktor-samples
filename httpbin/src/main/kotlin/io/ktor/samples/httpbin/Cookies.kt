package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.openapi.GenericElement
import io.ktor.openapi.jsonSchema
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
        summary = "Returns cookie data."
        responses {
            HttpStatusCode.OK {
                description = "Set cookies."
                schema = schemaWithExamples<CookiesResponse>("CookiesResponse")
            }
        }
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
        summary = "Deletes cookie(s) as provided by the query string and redirects to cookie list."
        responses {
            HttpStatusCode.OK {
                description = "Redirect to cookie list."
                schema = schemaWithExamples<CookiesResponse>("CookiesResponse")
            }
        }
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
        summary = "Sets cookie(s) as provided by the query string and redirects to cookie list."

        parameters {
            query("freeform") {
                required = false
                schema = jsonSchema<Map<String, String>>().copy(
                    example = GenericElement(
                        mapOf("cookie1" to "value1")
                    )
                )
                style = "form"
                explode = true
            }
        }
        responses {
            HttpStatusCode.OK {
                description = "Redirect to cookie list."
                schema = schemaWithExamples<CookiesResponse>("CookiesResponse")
            }
        }
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
        summary = "Sets a cookie and redirects to cookie list."

        responses {
            HttpStatusCode.OK {
                description = "Set cookies and redirects to cookie list."
                schema = schemaWithExamples<CookiesResponse>("CookiesResponse")
            }
        }
    }
}