package io.ktor.samples.mpp.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal expect val ApplicationDispatcher: CoroutineDispatcher

class ApplicationApi {
    private val client = HttpClient()

    private val address = Url("https://cors-test.appspot.com/test")

    @OptIn(DelicateCoroutinesApi::class)
    fun about(callback: (String) -> Unit) {
        GlobalScope.launch(ApplicationDispatcher) {
            val result: String = client.get {
                url(address.toString())
            }.bodyAsText()

            callback(result)
        }
    }
}
