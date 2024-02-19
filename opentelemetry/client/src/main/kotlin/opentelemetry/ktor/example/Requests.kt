package opentelemetry.ktor.example

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*


suspend fun doRequests(client: HttpClient) {
    // For this request you can see `CUSTOM` method instead of default `HTTP` in the Jaeger UI
    client.request("/known-methods") {
        method = CUSTOM_METHOD
    }

    // For this request you can't see `CUSTOM_NOT_KNOWN` method, you can see default `HTTP` in the Jaeger UI
    client.request("/known-methods") {
        method = CUSTOM_METHOD_NOT_KNOWN
    }

    // You can see tags `http.request.header.accept` and `http.response.header.content_type` for all requests
    // in the Jaeger UI and also `http.response.header.custom_header` for this request
    client.get("/captured-headers")

    // For this request you can see tag `error=true` and `Error` icon only for server trace in the Jaeger UI
    client.get("/span-status-extractor")

    // For this request you can see tag `span.kind=producer` only for server trace in the Jaeger UI
    client.post("/span-kind-extractor")

    // You can see attribute `start-time` and `end-time` in the Jaeger UI for all requests
    client.get("/attribute-extractor")

    // For this request you can see several spans and events only for server trace in the Jaeger UI
    client.get("/opentelemetry/tracer")

    // For this request you can see several events only for server trace in the Jaeger UI
    client.ws("/opentelemetry/websocket") {
        send(Frame.Text("Hello, world!"))
        repeat(10) {
            send(incoming.receive())
        }
    }
}