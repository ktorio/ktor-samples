package io.ktor.samples.httpbin

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.accept
import io.ktor.server.response.respond
import io.ktor.server.response.respondResource
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.images() {
    get("/image") {
        val serverAccepts = listOf("image/webp", "image/svg+xml", "image/jpeg", "image/png", "image/*")

        val clientAccepts = call.request.accept()
            ?.split(",")
            ?.map(String::trim) ?: emptyList()

        val accepts = clientAccepts.intersect(serverAccepts)

        if (accepts.isEmpty()) {
            call.respond(
                HttpStatusCode.NotAcceptable,
                ImageErrorResponse(
                    message = "Client did not request a supported media type",
                    accept = serverAccepts
                ),
            )
            return@get
        }

        when (accepts.first()) {
            "image/webp" -> {
                call.respondResource("sample.webp")
            }
            "image/svg+xml" -> {
                call.respondResource("sample.svg")
            }
            "image/jpeg" -> {
                call.respondResource("sample.jpg")
            }
            "image/png", "image/*" -> {
                call.respondResource("sample.png")
            }
        }
    }.describe {
        tag("Images")

        summary = "Returns a simple image of the type suggest by the Accept header."
        responses {
            HttpStatusCode.OK {
                description = "An image."
                ContentType.Image.WEBP()
                ContentType.Image.SVG()
                ContentType.Image.JPEG()
                ContentType.Image.PNG()
                ContentType.Image.Any()
            }
        }
    }

    get("/image/jpeg") {
        call.respondResource("sample.jpg")
    }.describe {
        tag("Images")
        summary = "Returns a simple JPEG image."
        responses {
            HttpStatusCode.OK {
                description = "A JPEG image."
                ContentType.Image.JPEG()
            }
        }
    }
    get("/image/png") {
        call.respondResource("sample.png")
    }.describe {
        tag("Images")
        summary = "Returns a simple PNG image."
        responses {
            HttpStatusCode.OK {
                description = "A PNG image."
                ContentType.Image.PNG()
            }
        }
    }
    get("/image/svg") {
        call.respondResource("sample.svg")
    }.describe {
        tag("Images")
        summary = "Returns a simple SVG image."
        responses {
            HttpStatusCode.OK {
                description = "An SVG image."
                ContentType.Image.SVG()
            }
        }
    }
    get("/image/webp") {
        call.respondResource("sample.webp")
    }.describe {
        tag("Images")
        summary = "Returns a simple WEBP image."
        responses {
            HttpStatusCode.OK {
                description = "A WEBP image."
                ContentType.Image.WEBP()
            }
        }
    }
}