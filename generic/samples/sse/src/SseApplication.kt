package io.ktor.samples.sse

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * SSE (Server-Sent Events) sample application.
 * This is the main entrypoint of the application.
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
fun main(args: Array<String>) {
    /**
     * Here we create and start a Netty embedded server listening to the port 8080
     * and define the main application module inside the specified lambda.
     */
    embeddedServer(Netty, port = 8080) { // this: Application ->

        /**
         * We produce a [BroadcastChannel] from a suspending function
         * that send a [SseEvent] instance each second.
         */
        val channel = produce { // this: ProducerScope<SseEvent> ->
            var n = 0
            while (true) {
                send(SseEvent("demo$n"))
                delay(1000)
                n++
            }
        }.broadcast()

        /**
         * We use the [Routing] feature to declare [Route] that will be
         * executed per call
         */
        routing {
            /**
             * Route to be executed when the client perform a GET `/sse` request.
             * It will respond using the [respondSse] extension method defined in this same file
             * that uses the [BroadcastChannel] channel we created earlier to emit those events.
             */
            get("/sse") {
                val events = channel.openSubscription()
                try {
                    call.respondSse(events)
                } finally {
                    events.cancel()
                }
            }
            /**
             * Route to be executed when the client perform a GET `/` request.
             * It will serve a HTML file embedded directly in this string that
             * contains JavaScript code to connect to the `/sse` endpoint using
             * the EventSource JavaScript class ( https://html.spec.whatwg.org/multipage/comms.html#the-eventsource-interface ).
             * Normally you would serve HTML and JS files using the [static] method.
             * But for illustrative reasons we are embedding this here.
             */
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
        }
    }.start(wait = true)
}

/**
 * The data class representing a SSE Event that will be sent to the client.
 */
data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

/**
 * Method that responds an [ApplicationCall] by reading all the [SseEvent]s from the specified [events] [ReceiveChannel]
 * and serializing them in a way that is compatible with the Server-Sent Events specification.
 *
 * You can read more about it here: https://www.html5rocks.com/en/tutorials/eventsource/basics/
 */
suspend fun ApplicationCall.respondSse(events: ReceiveChannel<SseEvent>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondTextWriter(contentType = ContentType.Text.EventStream) {
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
