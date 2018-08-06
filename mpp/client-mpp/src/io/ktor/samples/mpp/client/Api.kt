package io.ktor.samples.mpp.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.experimental.*

internal expect val ApplicationDispatcher: CoroutineDispatcher

class ApplicationApi {
    private val client = HttpClient()

    fun about(callback: (String) -> Unit) {
        launch(ApplicationDispatcher) {
            val result: String = client.get {
                url {
                    protocol = URLProtocol.HTTPS
                    port = 443
                    host = "tools.ietf.org"
                    encodedPath = "rfc/rfc1866.txt"
                }
            }

            callback(result)
        }
    }
}
