package io.ktor.samples.reverseproxy

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.response.*
import io.ktor.content.TextContent
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.experimental.io.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        val client = HttpClient()
        val wikipediaLang = "en"

        intercept(ApplicationCallPipeline.Call) {
            val result = client.call("https://$wikipediaLang.wikipedia.org${call.request.uri}")
            val proxiedHeaders = result.response.headers
            val location = proxiedHeaders[HttpHeaders.Location]
            val contentType = proxiedHeaders[HttpHeaders.ContentType]
            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]

            fun String.stripWikipediaDomain() = this.replace(Regex("(https?:)?//\\w+\\.wikipedia\\.org"), "")

            // Propagates location header, removing wikipedia domain from it
            if (location != null) {
                call.response.header(HttpHeaders.Location, location.stripWikipediaDomain())
            }

            when {
                (contentType ?: "").startsWith("text/html") -> {
                    val text = result.response.readText()
                    val filteredText = text.stripWikipediaDomain()
                    call.respond(
                        TextContent(
                            filteredText,
                            ContentType.Text.Html.withCharset(Charsets.UTF_8),
                            result.response.status
                        )
                    )
                }
                else -> {
                    call.respond(object : OutgoingContent.WriteChannelContent() {
                        override val contentLength: Long? = contentLength?.toLong()
                        override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
                        override val headers: Headers = Headers.build {
                            appendAll(proxiedHeaders.filter { key, _ -> !key.equals(HttpHeaders.ContentType, ignoreCase = true) && !key.equals(HttpHeaders.ContentLength, ignoreCase = true) })
                        }
                        override val status: HttpStatusCode? = result.response.status
                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            result.response.content.copyAndClose(channel)
                        }
                    })
                }
            }
        }
    }
    server.start(wait = true)
}
