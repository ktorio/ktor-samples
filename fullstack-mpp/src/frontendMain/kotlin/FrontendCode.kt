package io.ktor.samples.fullstack.frontend

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.samples.fullstack.common.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*

private val client = HttpClient(Js)
private val scope = MainScope()
private val endpoint = window.location.origin

@Suppress("unused")
@JsName("helloWorld")
fun helloWorld(salutation: String) {
    val message = "$salutation from Kotlin.JS ${getCommonWorldString()}"
    document.getElementById("js-response")?.textContent = message

    scope.launch {
        delay(3000)
        document.getElementById("js-response")?.textContent = "making request..."
        delay(3000)
        val response = client.get {
            url.takeFrom(endpoint);
            url.appendPathSegments(listOf("test"))
        }
        document.getElementById("js-response")?.textContent = """result is: "$response""""
    }
}

fun main() {
    document.addEventListener("DOMContentLoaded", {
        helloWorld("Hi!")
    })
}