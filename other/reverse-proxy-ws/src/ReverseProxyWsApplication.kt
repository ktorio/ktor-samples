package io.ktor.samples.reverseproxyws

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.client.features.websocket.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.intellij.lang.annotations.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        install(io.ktor.websocket.WebSockets)
        routing {
            get("/") {
                @Language("es6")
                val js = """
                    const repliesDiv = document.getElementById('replies');
                    const messageInput = document.getElementById('message');
                    const sendMessageButton = document.getElementById('sendMessage');

                    function addReply(text) {
                        const div = document.createElement("div")
                        div.innerText = text;
                        repliesDiv.appendChild(div);
                    }

                    const ws = new WebSocket("ws://127.0.0.1:${call.request.origin.port}")
                    ws.onopen = (e) => { addReply("Connected"); };
                    ws.onclose = (e) => { addReply("Disconnected"); };
                    ws.onerror = (e) => { addReply("Error " + e); };
                    ws.onmessage = (e) => { addReply("Received: " + e.data); };

                    sendMessageButton.onclick = (e) => {
                        const message = messageInput.value;
                        messageInput.value = "";
                        ws.send(message);
                    };
                """.trimIndent()

                call.respondText("""
                    <html>
                        <body>
                            <form action="javascript:void(0)" method="post">
                                <input id="message" type="text" autofocus />
                                <input id="sendMessage" type="submit" value="Send" />
                            </form>
                            <pre id="replies"></pre>
                            <script type="text/javascript">$js</script>
                        </body>
                    </html>
                """.trimIndent(), ContentType.Text.Html)
            }
            //webSocketReverseProxy("/", proxied = Url("wss://echo.websocket.org/?encoding=text")) // Not working (disconnecting)
            webSocketReverseProxy("/", proxied = Url("ws://echo.websocket.org/?encoding=text"))
        }
    }.start(wait = true)
}

fun Route.webSocketReverseProxy(path: String, proxied: Url) {
    webSocket(path) {
        val serverSession = this

        val client = HttpClient(CIO).config { install(io.ktor.client.features.websocket.WebSockets) {
        } }

        client.webSocket(call.request.httpMethod, proxied.host, 0, proxied.fullPath, request = {
            url.protocol = proxied.protocol
            url.port = proxied.port
            println("Connecting to: ${url.buildString()}")
        }) {
            val clientSession = this
            val serverJob = launch {
                serverSession.incoming.pipeTo(clientSession.outgoing)

                //// Or this:
                //for (received in serverSession.incoming) {
                //    clientSession.send(received)
                //}
            }

            val clientJob = launch {
                clientSession.incoming.pipeTo(serverSession.outgoing)

                //// Or this:
                //for (received in clientSession.incoming) {
                //    serverSession.send(received)
                //}
            }

            //clientSession.send(io.ktor.http.cio.websocket.Frame.Text("hello"))

            listOf(serverJob, clientJob).joinAll()
        }
    }
}

suspend fun <E> ReceiveChannel<E>.pipeTo(send: SendChannel<E>) = run { for (received in this) send.send(received) }
