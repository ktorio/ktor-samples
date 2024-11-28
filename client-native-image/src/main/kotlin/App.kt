package io.ktor.samples.client

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

suspend fun main() {
    val client = HttpClient()

    val response = client.get("https://ktor.io")
        .bodyAsText()

    println(response)
}