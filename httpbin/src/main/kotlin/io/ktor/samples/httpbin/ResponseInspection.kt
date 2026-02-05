package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.toHttpDate
import io.ktor.openapi.GenericElement
import io.ktor.openapi.jsonSchema
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.method
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.route
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ExperimentalKtorApi
import java.security.MessageDigest
import java.time.Instant
import java.util.Date
import kotlin.text.any

val headerRegex = """[A-Za-z0-9-_]+""".toRegex()

@OptIn(ExperimentalKtorApi::class)
fun Route.responseInspection() {
    get("/cache") {
        if (call.request.headers[HttpHeaders.IfModifiedSince] != null
            || call.request.headers[HttpHeaders.IfNoneMatch] != null) {

            call.respond(HttpStatusCode.NotModified)
            return@get
        }

        val builder = HttpbinResponse.Builder()
            .argsFromQuery(call.request.queryParameters)
            .setHeaders(call.request.headers)
            .setOrigin(call.request.local)
            .setURL(call.request)

        val body = builder.build()
        val jsonBody = prettyJson.encodeToString(body)

        val gmt = GMTDate(Date.from(Instant.now()).time)

        call.response.headers.append(HttpHeaders.LastModified, gmt.toHttpDate())
        call.response.headers.append(HttpHeaders.ETag, "\"${sha256Hex(jsonBody)}\"")

        call.respondText(jsonBody, contentType = ContentType.Application.Json)
    }.describe {
        tag("Response inspection")
        summary = "Returns a 304 if an If-Modified-Since header or If-None-Match is present. Returns the same as a GET otherwise."
        parameters {
            header("If-Modified-Since") {
                description = "If-Modified-Since"
            }
            header("If-None-Match") {
                description = "If-None-Match"
            }
        }
        responses {
            HttpStatusCode.OK {
                description = "Cached response"
                schema = schemaGet()
            }
            HttpStatusCode.NotModified {
                description = "Not modified"
            }
        }
    }

    get("/cache/{max-age}") {
        val maxAge = call.parameters["max-age"]

        if (maxAge == null || maxAge.any { !it.isDigit() }) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val builder = HttpbinResponse.Builder()
            .argsFromQuery(call.request.queryParameters)
            .setHeaders(call.request.headers)
            .setOrigin(call.request.local)
            .setURL(call.request)

        val body = builder.build()
        val jsonBody = prettyJson.encodeToString(body)

        val gmt = GMTDate(Date.from(Instant.now()).time)

        call.response.headers.append(HttpHeaders.LastModified, gmt.toHttpDate())
        call.response.headers.append(HttpHeaders.ETag, "\"${sha256Hex(jsonBody)}\"")
        call.response.headers.append(HttpHeaders.CacheControl, "public, max-age=$maxAge")
        call.respondText(jsonBody, contentType = ContentType.Application.Json)
    }.describe {
        tag("Response inspection")
        summary = "Sets a Cache-Control header for n seconds."
        responses {
            HttpStatusCode.OK {
                description = "Cache control set"
                schema = schemaGet()
            }
        }
    }

    get("/etag/{etag}") {
        val noneMatch = call.request.headers[HttpHeaders.IfNoneMatch]
        val etag = call.parameters["etag"] ?: ""

        if (noneMatch != null && etag == noneMatch) {
            call.response.headers.append(HttpHeaders.ETag, "\"$etag\"")
            call.respond(HttpStatusCode.NotModified)
            return@get
        } else {
            val ifMatch = call.request.headers[HttpHeaders.IfMatch]
            if (ifMatch != null && ifMatch != etag) {
                call.respond(HttpStatusCode.PreconditionFailed)
                return@get
            }
        }

        val builder = HttpbinResponse.Builder()
            .argsFromQuery(call.request.queryParameters)
            .setHeaders(call.request.headers)
            .setOrigin(call.request.local)
            .setURL(call.request)

        call.response.headers.append(HttpHeaders.ETag, "\"$etag\"")
        call.respond(builder.build())
    }.describe {
        tag("Response inspection")
        summary = "Assumes the resource has the given etag and responds to If-None-Match and If-Match headers appropriately."
        parameters {
            header("If-None-Match") {
                description = "If-None-Match"
            }

            header("If-Match") {
                description = "If-Match"
            }
        }
        responses {
            HttpStatusCode.OK {
                description = "Normal response"
                schema = schemaGet()
            }
            HttpStatusCode.PreconditionFailed {
                description = "Precondition failed"
            }
        }
    }

    route("/response-headers") {
        for (method in listOf(HttpMethod.Get, HttpMethod.Post)) {
            method(method) {
                handle {
                    if (call.request.httpMethod != HttpMethod.Get && call.request.httpMethod != HttpMethod.Post) {
                        call.respond(HttpStatusCode.NotFound)
                        return@handle
                    }

                    val headers = mutableMapOf<String, List<String>>()
                    val customHeaders = mutableListOf<Pair<String, String>>()

                    for ((name, values) in call.request.queryParameters.entries()) {
                        if (values.isEmpty()) {
                            customHeaders.add(name to "")
                        } else {
                            for (v in values) {
                                customHeaders.add(name to v)
                            }
                        }

                        headers[name] = values
                    }

                    val invalidHeaders = customHeaders.filter { (name, _) -> !headerRegex.matches(name) }

                    if (invalidHeaders.isNotEmpty()) {
                        call.respondText("Invalid HTTP header name: \"${invalidHeaders.first().first}\"", status = HttpStatusCode.BadRequest)
                        return@handle
                    }

                    headers["Content-Type"] = listOf("application/json")
                    headers["Content-Length"] = listOf("0")
                    val bytes = prettyJson.encodeToString(SmartValueMapSerializer, headers).toByteArray()
                    headers["Content-Length"] = listOf((bytes.size + (bytes.size.toString().length - 1)).toString())

                    for ((name, value) in customHeaders) {
                        call.response.headers.append(name, value)
                    }

                    call.respondText(
                        prettyJson.encodeToString(SmartValueMapSerializer, headers.toSortedMap()),
                        contentType = ContentType.Application.Json
                    )
                }
            }.describe {
                tag("Response inspection")
                summary = "Returns a set of response headers from the query string."
                parameters {
                    query("freeform") {
                        required = false
                        schema = jsonSchema<Map<String, String>>().copy(
                            example = GenericElement(
                                mapOf("Header" to "Value")
                            )
                        )
                        style = "form"
                        explode = true
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Response headers"
                        schema = jsonSchema<Map<String, String>>().copy(
                            example = GenericElement(
                                mapOf(
                                    "Content-Type" to "application/json",
                                    "Content-Length" to "123"
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

fun sha256Hex(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(input.toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }
}