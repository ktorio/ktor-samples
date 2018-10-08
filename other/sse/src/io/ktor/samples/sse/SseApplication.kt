package io.ktor.samples.sse

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        val channel = produce {
            var n = 0
            while (true) {
                send(SseEvent("demo${n++}"))
                delay(1000)
            }
        }.broadcast()

        routing {
            get("/") {
                call.respondText(
                    """
                        <html>
                            <head></head>
                            <body>
                                <ul id="events">
                                </ul>
                                <script type="text/javascript">
                                    var source = new EventSource('/sse');
                                    var eventsUl = document.getElementById('events');

                                    function logEvent(text) {
                                        var li = document.createElement('li')
                                        li.innerText = text;
                                        eventsUl.appendChild(li);
                                    }

                                    source.addEventListener('message', function(e) {
                                        logEvent('message:' + e.data);
                                    }, false);

                                    source.addEventListener('open', function(e) {
                                        logEvent('open');
                                    }, false);

                                    source.addEventListener('error', function(e) {
                                        if (e.readyState == EventSource.CLOSED) {
                                            logEvent('closed');
                                        } else {
                                            logEvent('error');
                                            console.log(e);
                                        }
                                    }, false);
                                </script>
                            </body>
                        </html>
                    """.trimIndent(),
                    contentType = ContentType.Text.Html
                )
            }
            get("/sse") {
                call.respondSse(channel.openSubscription())
            }
        }
    }.start(wait = true)
}

data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

suspend fun ApplicationCall.respondSse(events: ReceiveChannel<SseEvent>) {
    response.header(HttpHeaders.CacheControl, "no-cache")
    respondTextWriter(contentType = ContentTypeTextEventStream) {
        for (event in events) {
            if (event.id != null) {
                write("id: ${event.id}\n")
            }
            if (event.event != null) {
                write("event: ${event.event}\n")
            }
            for (dataLine in event.data.lines()) {
                write("data: $dataLine\n")
            }
            write("\n")
            flush()
        }
    }
}

val ContentTypeTextEventStream = ContentType.parse("text/event-stream")
