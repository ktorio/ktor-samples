package io.ktor.samples.mpp.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
internal expect val ApplicationDispatcher: CoroutineDispatcher

class ApplicationApi {
    private val client = HttpClient()

    private val address = Url("https://cors-test.appspot.com/test")

    fun about(callback: (String) -> Unit) {
        GlobalScope.launch(ApplicationDispatcher) {
            val result: String = client.get {
                url(address.toString())
            }.bodyAsText()

            callback(result)
        }
    }
}
