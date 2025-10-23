package io.ktor.samples.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

suspend fun main() {
    val apacheClient = HttpClient(Apache)
    val apacheResponse = apacheClient.get("https://ktor.io").bodyAsText()
    println("$apacheClient: ${apacheResponse.take(100)}")


    val cioClient = HttpClient(CIO)
    val cioResponse = cioClient.get("https://ktor.io").bodyAsText()
    println("$cioClient: ${cioResponse.take(100)}")
}