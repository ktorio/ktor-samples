package io.ktor.samples.mpp.client

import io.ktor.http.*
import kotlin.browser.*

fun main() {
    ApplicationApi().apply {
        this.address = Url("https://ktor.io/pages.txt") // The default URL doesn't support CORS
    }.about {
        val div = document.createElement("pre")
        div.textContent = it
        document.body?.appendChild(div)
    }
}

