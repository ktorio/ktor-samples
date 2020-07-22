package io.ktor.samples.chat.frontend

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSocketException
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document
import kotlin.browser.window

@KtorExperimentalAPI
fun main() {
    val wsClient = WsClient(HttpClient { install(WebSockets) })
    GlobalScope.launch { initConnection(wsClient) }

    document.addEventListener("DOMContentLoaded", {
        val sendBtn = document.getElementById("sendButton") as HTMLElement
        val commandInput = document.getElementById("commandInput") as HTMLInputElement

        sendBtn.addEventListener("click", {
            GlobalScope.launch { sendMessage(wsClient, commandInput) }
        })
        commandInput.addEventListener("keydown", { e ->
            if ((e as KeyboardEvent).key == "Enter") {
                GlobalScope.launch { sendMessage(wsClient, commandInput) }
            }
        })
    })
}

suspend fun initConnection(wsClient: WsClient) {
    try {
        wsClient.connect()
        wsClient.receive(::writeMessage)
    } catch (e: Exception) {
        if (e is ClosedReceiveChannelException) {
            writeMessage("Disconnected. ${e.message}.")
        } else if (e is WebSocketException) {
            writeMessage("Unable to connect.")
        }

        window.setTimeout({
            GlobalScope.launch { initConnection(wsClient) }
        }, 5000)
    }
}

suspend fun sendMessage(client: WsClient, input: HTMLInputElement) {
    if (input.value.isNotEmpty()) {
        client.send(input.value)
        input.value = ""
    }
}

fun writeMessage(message: String) {
    val line = document.createElement("p") as HTMLElement
    line.className = "message"
    line.textContent = message

    val messagesBlock = document.getElementById("messages") as HTMLElement
    messagesBlock.appendChild(line)
    messagesBlock.scrollTop = line.offsetTop.toDouble()
}

class WsClient(private val client: HttpClient) {
    var session: WebSocketSession? = null

    suspend fun connect() {
        session = client.webSocketSession(
            method = HttpMethod.Get,
            host = window.location.hostname,
            port = window.location.port.toInt(),
            path = "/ws"
        )
    }

    suspend fun send(message: String) {
        session?.send(Frame.Text(message))
    }

    suspend fun receive(onReceive: (input: String) -> Unit) {
        while(true) {
            val frame = session?.incoming?.receive()

            if (frame is Frame.Text) {
                onReceive(frame.readText())
            }
        }
    }
}