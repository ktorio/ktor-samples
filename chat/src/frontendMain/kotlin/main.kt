package io.ktor.samples.chat.frontend

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val wsClient = WsClient(HttpClient { install(WebSockets) })
    GlobalScope.launch { initConnection(wsClient) }

    document.addEventListener("DOMContentLoaded", {
        val sendButton = document.getElementById("sendButton") as HTMLElement
        val commandInput = document.getElementById("commandInput") as HTMLInputElement

        sendButton.addEventListener("click", {
            GlobalScope.launch { sendMessage(wsClient, commandInput) }
        })
        commandInput.addEventListener("keydown", { e ->
            if ((e as KeyboardEvent).key == "Enter") {
                GlobalScope.launch { sendMessage(wsClient, commandInput) }
            }
        })
    })
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun initConnection(wsClient: WsClient) {
    try {
        wsClient.connect()
        wsClient.receive(::writeMessage)
    } catch (e: Exception) {
        when (e) {
            is ClosedReceiveChannelException -> writeMessage("Disconnected. ${e.message}")
            is WebSocketException -> writeMessage("Unable to connect.")
            else -> writeMessage("Unexpected error: ${e.message}")
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
//        while (true) {
//            // null-safe 처리 및 수신 종료 처리
////            val frame = session?.incoming?.receive()
//
//            val s = session ?: return
//            val frame = s.incoming.receive()
//
//
//            if (frame is Frame.Text) {
//                onReceive(frame.readText())
//            }
//        }
        val s = session ?: return
        try {
            while (true) {
                val frame = s.incoming.receive()
                if (frame is Frame.Text) {
                    onReceive(frame.readText())
                }
            }
        } finally {
            s.close()
        }
    }
}
