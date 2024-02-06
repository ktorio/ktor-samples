package opentelemetry.ktor.example

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*


suspend fun doRequests(client: HttpClient) {
    client.request("/known-methods") {
        method = CUSTOM_METHOD
    }

    client.request("/known-methods") {
        method = CUSTOM_METHOD_NOT_KNOWN
    }

    client.get("/captured-headers")

    client.get("/span-status-extractor")

    client.post("/span-kind-extractor")

    client.get("/attribute-extractor")

    client.get("/opentelemetry/tracer")

    client.ws("/opentelemetry/websocket") {
        send(Frame.Text("Hello, world!"))
        repeat(10) {
            send(incoming.receive())
        }
    }
}