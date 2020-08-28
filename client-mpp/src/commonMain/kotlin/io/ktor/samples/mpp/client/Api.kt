package io.ktor.samples.mpp.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*

internal expect val ApplicationDispatcher: CoroutineDispatcher

class ApplicationApi {
    private val client = HttpClient()

    var address = Url("https://tools.ietf.org/rfc/rfc1866.txt")

    fun about(callback: (String) -> Unit) {
        GlobalScope.apply {
            launch(ApplicationDispatcher) {
                val result: String = client.get {
                    url(this@ApplicationApi.address.toString())
                }

                callback(result)
            }
        }
    }
}
