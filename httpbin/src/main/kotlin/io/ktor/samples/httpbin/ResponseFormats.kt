package io.ktor.samples.httpbin

import com.aayushatharva.brotli4j.Brotli4jLoader
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.samples.httpbin.SampleModel.Slide
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondResource
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.util.DeflateEncoder
import io.ktor.util.GZipEncoder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ExperimentalKtorApi
import io.ktor.utils.io.toByteArray


@OptIn(ExperimentalKtorApi::class)
fun Route.responseFormats() {
    get("/brotli") {
        val body = prettyJson.encodeToString(
            BrotliResponse(
                brotli = true,
                headers = call.request.headers.toSortedMap(),
                method = call.request.httpMethod.value,
                origin = call.request.local.remoteAddress
            )
        )

        Brotli4jLoader.ensureAvailability()
        val compressed = com.aayushatharva.brotli4j.encoder.Encoder.compress(body.toByteArray(Charsets.UTF_8))
        call.response.headers.append(HttpHeaders.ContentEncoding, "br")
        call.respondBytes(compressed, contentType = ContentType.Application.Json)
    }.describe {
        tag("Response formats")
        summary = "Returns Brotli-encoded data."
        responses {
            HttpStatusCode.OK {
                description = "Brotli-encoded data."
                schema = schemaWithExamples<BrotliResponse>("BrotliResponse")
            }
        }
    }

    get("/deflate") {
        val body = prettyJson.encodeToString(
            DeflateResponse(
                deflated = true,
                headers = call.request.headers.toSortedMap(),
                method = call.request.httpMethod.value,
                origin = call.request.local.remoteAddress
            )
        )

        val compressed = DeflateEncoder.encode(ByteReadChannel(body))

        call.response.headers.append(HttpHeaders.ContentEncoding, "deflate")
        call.respondBytes(compressed.toByteArray(), contentType = ContentType.Application.Json)
    }.describe {
        tag("Response formats")
        summary = "Returns Brotli-encoded data."
        responses {
            HttpStatusCode.OK {
                description = "Deflate-encoded data."
                schema = schemaWithExamples<DeflateResponse>("DeflateResponse")
            }
        }
    }

    get("/gzip") {
        val body = prettyJson.encodeToString(
            GzipResponse(
                gzipped = true,
                headers = call.request.headers.toSortedMap(),
                method = call.request.httpMethod.value,
                origin = call.request.local.remoteAddress
            )
        )

        val compressed = GZipEncoder.encode(ByteReadChannel(body))

        call.response.headers.append(HttpHeaders.ContentEncoding, "gzip")
        call.respondBytes(compressed.toByteArray(), contentType = ContentType.Application.Json)
    }.describe {
        tag("Response formats")
        summary = "Returns GZip-encoded data."
        responses {
            HttpStatusCode.OK {
                description = "GZip-encoded data."
                schema = schemaWithExamples<GzipResponse>("GzipResponse")
            }
        }
    }

    get("/deny") {
        call.respondText("""
          .-''''''-.
        .' _      _ '.
       /   O      O   \\
      :                :
      |                |
      :       __       :
       \  .-"`  `"-.  /
        '.          .'
          '-......-'
     YOU SHOULDN'T BE HERE
""".trimIndent(), contentType = ContentType.Text.Plain)
    }.describe {
        tag("Response formats")
        summary = "Returns page denied by robots.txt rules."
        responses {
            HttpStatusCode.OK {
                description = "Denied message"
                ContentType.Text.Plain {
                    schema = jsonSchema<String>()
                }
            }
        }
    }

    get("/encoding/utf8") {
        call.respondResource("utf8.html")
    }.describe {
        tag("Response formats")
        summary = "Returns a UTF-8 encoded body."
        responses {
            HttpStatusCode.OK {
                description = "Encoded UTF-8 content."
                ContentType.Text.Html {
                    schema = jsonSchema<String>()
                }
            }
        }
    }

    get("/html") {
        call.respondResource("sample.html")
    }.describe {
        tag("Response formats")
        summary = "Returns a simple HTML document."
        responses {
            HttpStatusCode.OK {
                description = "An HTML page."
                ContentType.Text.Html {
                    schema = jsonSchema<String>()
                }
            }
        }
    }

    get("/xml") {
        call.respondResource("sample.xml")
    }.describe {
        tag("Response formats")
        summary = "Returns a simple XML document."
        responses {
            HttpStatusCode.OK {
                description = "An XML document."
                ContentType.Application.Xml {
                    schema = jsonSchema<String>()
                }
            }
        }
    }

    get("/json") {
        call.respond(
            SampleModel(
                SampleModel.SlideShow(
                    author = "Yours Truly",
                    date = "date of publication",
                    slides = listOf(
                        Slide("Wake up to WonderWidgets!", "all"),
                        Slide(
                            "Overview", "all", listOf(
                                "Why <em>WonderWidgets</em> are great",
                                "Who <em>buys</em> WonderWidgets"
                            )
                        )
                    ),
                    title = "Sample Slide Show"
                )
            )
        )
    }.describe {
        tag("Response formats")
        summary = "Returns a simple JSON document."
        responses {
            HttpStatusCode.OK {
                description = "An JSON document."
                schema = jsonSchema<SampleModel>()
            }
        }
    }

    get("/robots.txt") {
        call.respondText("User-agent: *\nDisallow: /deny", contentType = ContentType.Text.Plain)
    }.describe {
        tag("Response formats")
        summary = "Returns some robots.txt rules."
        responses {
            HttpStatusCode.OK {
                description = "Robots file"
                ContentType.Text.Plain {
                    schema = jsonSchema<String>()
                }
            }
        }
    }
}