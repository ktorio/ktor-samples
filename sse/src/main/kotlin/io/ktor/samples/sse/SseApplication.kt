package io.ktor.samples.sse

import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

/**
 * An SSE (Server-Sent Events) sample application.
 * This is the main entrypoint of the application.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    /**
     * Here we create and start a Netty embedded server listening to the port 8080
     * and define the main application module.
     */
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

/**
 * The data class representing a SSE Event that will be sent to the client.
 */
data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

/**
 * Method that responds an [ApplicationCall] by reading all the [SseEvent]s from the specified [eventFlow] [Flow]
 * and serializing them in a way that is compatible with the Server-Sent Events specification.
 *
 * You can read more about it here: https://www.html5rocks.com/en/tutorials/eventsource/basics/
 */
suspend fun ApplicationCall.respondSse(eventFlow: Flow<SseEvent>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondBytesWriter(contentType = ContentType.Text.EventStream) {
        eventFlow.collect { event ->
            if (event.id != null) {
                writeStringUtf8("id: ${event.id}\n")
            }
            if (event.event != null) {
                writeStringUtf8("event: ${event.event}\n")
            }
            for (dataLine in event.data.lines()) {
                writeStringUtf8("data: $dataLine\n")
            }
            writeStringUtf8("\n")
            flush()
        }
    }
}

fun Application.module() {
    /**
     * We produce a [SharedFlow] from a function
     * that sends an [SseEvent] instance each second.
     */
    val sseFlow = flow {
        var n = 0
        while (true) {
            emit(SseEvent("demo$n"))
            delay(1.seconds)
            n++
        }
    }.shareIn(GlobalScope, SharingStarted.Eagerly)

    /**
     * We use the [Routing] plugin to declare [Route] that will be
     * executed per call
     */
    routing {
        /**
         * Route to be executed when the client perform a GET `/sse` request.
         * It will respond using the [respondSse] extension method defined in this same file
         * that uses the [SharedFlow] to collect sse events.
         */
        get("/sse") {
            call.respondSse(sseFlow)
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
}
