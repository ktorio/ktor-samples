package io.ktor.samples.fullstack.frontend

import io.ktor.samples.fullstack.common.*
import kotlin.browser.*

@Suppress("unused")
@JsName("helloWorld")
fun helloWorld(salutation: String) {
    val message = "$salutation from Kotlin.JS ${getCommonWorldString()}"
    document.getElementById("js-response")?.textContent = message
}

fun main() {
    document.addEventListener("DOMContentLoaded", {
        helloWorld("Hi!")
    })
}