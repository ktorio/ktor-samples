package io.ktor.samples.httpbin

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import kotlin.math.abs

private val imageContentTypes = listOf(
    ContentType.Image.WEBP,
    ContentType.Image.SVG,
    ContentType.Image.JPEG,
    ContentType.Image.PNG,
    ContentType.Image.Any
)

data class Accept(val contentType: ContentType, val quality: Double, val specificity: Int, val order: Int)

private fun chooseRepresentation(acceptHeader: String?, reprs: List<ContentType>): Pair<ContentType, Accept>? {
    val accepts = parseHeaderValue(acceptHeader).mapIndexed { index, value ->
        try {
            val contentType = ContentType.parse(value.value)

            Accept(
                contentType = contentType,
                quality = value.quality,
                specificity = when {
                    contentType.contentType == "*" && contentType.contentSubtype == "*" -> 0
                    contentType.contentSubtype == "*" -> 1
                    else -> 2
                },
                order = index
            )
        } catch (_: BadContentTypeFormatException) {
            null
        }
    }.filterNotNull()

    val epsilon = 1e-9
    val result = mutableListOf<Pair<ContentType, Accept?>>()
    for (contentType in reprs) {
        val matched = accepts.filter { contentType.match(it.contentType) }
        val maxQuality = matched.maxByOrNull { it.quality }?.quality
        val entry = if (maxQuality != null) {
            matched.filter { abs(it.quality - maxQuality) < epsilon }
                .maxWithOrNull { a, b ->
                    when {
                        a.specificity != b.specificity -> a.specificity.compareTo(b.specificity)
                        else -> b.order.compareTo(a.order)
                    }
                }
        } else {
            null
        }

        result.add(contentType to entry)
    }

    val chosen = result.filter { (_, e) -> e != null && e.quality > 0 }
        .maxWithOrNull { (_, a), (_, b) ->
            when {
                a!!.quality != b!!.quality -> a.quality.compareTo(b.quality)
                a.specificity != b.specificity -> a.specificity.compareTo(b.specificity)
                else -> b.order.compareTo(a.order)
            }
        }

    if (chosen == null) {
        return null
    }

    val (contentType, accept) = chosen

    if (accept == null) {
        return null
    }

    return contentType to accept
}


@OptIn(ExperimentalKtorApi::class)
fun Route.images() {
    get("/image") {
        val representation = chooseRepresentation(call.request.accept(), imageContentTypes)

        if (representation == null) {
            call.respond(
                HttpStatusCode.NotAcceptable,
                ImageErrorResponse(
                    message = "Client did not request a supported media type",
                    accept = imageContentTypes.map { it.toString() }
                ),
            )
            return@get
        }

        when (representation.first) {
            ContentType.Image.WEBP -> {
                call.respondResource("sample.webp")
            }
            ContentType.Image.SVG -> {
                call.respondResource("sample.svg")
            }
            ContentType.Image.JPEG -> {
                call.respondResource("sample.jpg")
            }
            ContentType.Image.PNG, ContentType.Image.Any -> {
                call.respondResource("sample.png")
            }
        }
    }.describe {
        tag("Images")

        summary = "Returns a simple image of the type suggest by the Accept header."
        responses {
            HttpStatusCode.OK {
                description = "An image."
                for (contentType in imageContentTypes) {
                    contentType()
                }
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