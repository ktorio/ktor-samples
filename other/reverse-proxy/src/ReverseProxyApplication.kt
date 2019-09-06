package io.ktor.samples.reverseproxy

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.TextContent
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.utils.io.*

/**
 * Main entry point of the application. This application starts a webserver at port 8080 based on Netty.
 * It intercepts all the requests, reverse-proxying them to the wikipedia.
 *
 * In the case of HTML it is completely loaded in memory and preprocessed to change URLs to our own local domain.
 * In the case of other files, the file is streamed from the HTTP client to the HTTP server response.
 */
fun main(args: Array<String>) {
    // Creates a Netty server
    val server = embeddedServer(Netty, port = 8080) {
        // Creates a new HttpClient
        val client = HttpClient()
        val wikipediaLang = "en"

        // Let's intercept all the requests at the [ApplicationCallPipeline.Call] phase.
        intercept(ApplicationCallPipeline.Call) {
            // We create a GET request to the wikipedia domain and return the call (with the request and the unprocessed response).
            val response = client.request<HttpResponse>("https://$wikipediaLang.wikipedia.org${call.request.uri}")

            // Get the relevant headers of the client response.
            val proxiedHeaders = response.headers
            val location = proxiedHeaders[HttpHeaders.Location]
            val contentType = proxiedHeaders[HttpHeaders.ContentType]
            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]

            // Extension method to process all the served HTML documents
            fun String.stripWikipediaDomain() = this.replace(Regex("(https?:)?//\\w+\\.wikipedia\\.org"), "")

            // Propagates location header, removing the wikipedia domain from it
            if (location != null) {
                call.response.header(HttpHeaders.Location, location.stripWikipediaDomain())
            }

            // Depending on the ContentType, we process the request one way or another.
            when {
                // In the case of HTML we download the whole content and process it as a string replacing
                // wikipedia links.
                contentType?.startsWith("text/html") == true -> {
                    val text = response.readText()
                    val filteredText = text.stripWikipediaDomain()
                    call.respond(
                        TextContent(
                            filteredText,
                            ContentType.Text.Html.withCharset(Charsets.UTF_8),
                            response.status
                        )
                    )
                }
                else -> {
                    // In the case of other content, we simply pipe it. We return a [OutgoingContent.WriteChannelContent]
                    // propagating the contentLength, the contentType and other headers, and simply we copy
                    // the ByteReadChannel from the HTTP client response, to the HTTP server ByteWriteChannel response.
                    call.respond(object : OutgoingContent.WriteChannelContent() {
                        override val contentLength: Long? = contentLength?.toLong()
                        override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
                        override val headers: Headers = Headers.build {
                            appendAll(proxiedHeaders.filter { key, _ -> !key.equals(HttpHeaders.ContentType, ignoreCase = true) && !key.equals(HttpHeaders.ContentLength, ignoreCase = true) })
                        }
                        override val status: HttpStatusCode? = response.status
                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            response.content.copyAndClose(channel)
                        }
                    })
                }
            }
        }
    }
    // Starts the server and waits for the engine to stop and exits.
    server.start(wait = true)
}
