package io.ktor.samples.auth

import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.io.*
import java.util.*
import kotlin.coroutines.experimental.*

class TestHttpClientFactory : HttpClientEngineFactory<TestHttpClientFactory.Config> {
    fun addResponse(url: String, headers: HeadersBuilder.() -> Unit = {}, response: (HttpRequest) -> OutgoingContent) {
        config.responses[url] = FakeResponse(
            response
        ) { respBody ->
            appendAll(respBody.headers)
            headers()
        }
    }

    data class FakeResponse(val body: (HttpRequest) -> OutgoingContent, val headers: HeadersBuilder.(OutgoingContent) -> Unit)

    class Config : HttpClientEngineConfig() {
        val responses = LinkedHashMap<String, FakeResponse>()
    }

    val config = Config()

    override fun create(block: TestHttpClientFactory.Config.() -> Unit): HttpClientEngine {
        val config = config.apply(block)
        return Engine(config)
    }

    class Engine(val config: TestHttpClientFactory.Config) : HttpClientEngine {
        override val dispatcher: CoroutineDispatcher = DefaultDispatcher

        override fun close() = Unit

        override suspend fun execute(call: HttpClientCall, data: HttpRequestData): HttpEngineCall {
            val context = coroutineContext
            val url = data.url.fullUrl
            val response = config.responses[url] ?: error("Can't find response for $url")

            val request = object : HttpRequest {
                override val attributes: Attributes = Attributes().apply { data.attributes(this) }
                override val call: HttpClientCall = call
                override val content: OutgoingContent = data.body as OutgoingContent
                override val executionContext: Job = Job()
                override val headers: Headers = data.headers
                override val method: HttpMethod = data.method
                override val url: Url = data.url
            }
            val body = response.body(request)

            return HttpEngineCall(
                request,
                object : HttpResponse {
                    override val call: HttpClientCall = call
                    override val content: ByteReadChannel = writer(context) {
                        when (body) {
                            is OutgoingContent.NoContent -> Unit
                            is OutgoingContent.ByteArrayContent -> channel.writeFully(body.bytes())
                            is OutgoingContent.ReadChannelContent -> body.readFrom().copyAndClose(channel)
                            is OutgoingContent.WriteChannelContent -> body.writeTo(channel)
                        }
                    }.channel
                    override val executionContext: Job = Job()
                    override val headers: Headers = HeadersBuilder().apply { response.headers(this, body) }.build()
                    override val requestTime: Date = Date()
                    override val responseTime: Date = Date()
                    override val status: HttpStatusCode = body.status ?: HttpStatusCode.OK
                    override val version: HttpProtocolVersion = HttpProtocolVersion.HTTP_1_1
                    override fun close() = Unit
                }
            )
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
