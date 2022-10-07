package io.ktor.samples.chat.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.*
import java.time.*

/**
 * An entry point of the application.
 *
 * Notice that the fully qualified name of this function is `io.ktor.samples.chat.backend.ChatApplicationKt.main`.
 * For top level functions, the class name containing the method in the JVM is FileNameKt.
 *
 * The `Application.main` part is Kotlin idiomatic that specifies that the main method is
 * an extension of the [Application] class, and thus can be accessed like a normal member `myapplication.main()`.
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        ChatApplication().apply { main() }
    }.start(wait = true)
}

fun Application.main() {
    ChatApplication().apply { main() }
}

/**
 * In this case, we have a class holding our application state so it is not global and can be tested easier.
 */
class ChatApplication {
    /**
     * This class handles the logic of a [ChatServer].
     * With the standard handlers [ChatServer.memberJoin] or [ChatServer.memberLeft] and operations like
     * sending messages to everyone or to specific people connected to the server.
     */
    private val server = ChatServer()

    /**
     * This is the main method of application in this class.
     */
    fun Application.main() {
        /**
         * First, we install the plugins we need.
         * They are bound to the whole application
         * since this method has an implicit [Application] receiver that supports the [install] method.
         */
        // This adds Date and Server headers to each response, and would allow you to configure
        // additional headers served to each response.
        install(DefaultHeaders)
        // This uses the logger to log every call (request/response)
        install(CallLogging)
        // This installs the WebSockets plugin to be able to establish a bidirectional configuration
        // between the server and the client
        install(WebSockets) {
            pingPeriod = Duration.ofMinutes(1)
        }
        // This enables the use of sessions to keep information between requests/refreshes of the browser.
        install(Sessions) {
            cookie<ChatSession>("SESSION")
        }

        // This adds an interceptor that will create a specific session in each request if no session is available already.
        intercept(ApplicationCallPipeline.Plugins) {
            if (call.sessions.get<ChatSession>() == null) {
                call.sessions.set(ChatSession(generateNonce()))
            }
        }

        /**
         * Now we are going to define routes to handle specific methods + URLs for this application.
         */
        routing {

            // Defines a websocket `/ws` route that allows a protocol upgrade to convert a HTTP request/response request
            // into a bidirectional packetized connection.
            webSocket("/ws") { // this: WebSocketSession ->

                // First of all we get the session.
                val session = call.sessions.get<ChatSession>()

                // We check that we actually have a session. We should always have one,
                // since we have defined an interceptor before to set one.
                if (session == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                    return@webSocket
                }

                // We notify that a member joined by calling the server handler [memberJoin].
                // This allows associating the session ID to a specific WebSocket connection.
                server.memberJoin(session.id, this)

                try {
                    // We start receiving messages (frames).
                    // Since this is a coroutine, it is suspended until receiving frames.
                    // Once the connection is closed, this consumeEach will finish and the code will continue.
                    incoming.consumeEach { frame ->
                        // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                        // We are only interested in textual messages, so we filter it.
                        if (frame is Frame.Text) {
                            // Now it is time to process the text sent from the user.
                            // At this point, we have context about this connection,
                            // the session, the text and the server.
                            // So we have everything we need.
                            receivedMessage(session.id, frame.readText())
                        }
                    }
                } finally {
                    // Either if there was an error, or if the connection was closed gracefully,
                    // we notified the server that the member had left.
                    server.memberLeft(session.id, this)
                }
            }

            // This defines a block of static resources for the '/' path (since no path is specified and we start at '/')
            static {
                // This marks index.html from the 'web' folder in resources as the default file to serve.
                defaultResource("index.html", "web")
                // This serves files from the 'web' folder in the application resources.
                resources("web")
            }

        }
    }

    /**
     * A chat session is identified by a unique nonce ID. This nonce comes from a secure random source.
     */
    data class ChatSession(val id: String)

    /**
     * We received a message. Let's process it.
     */
    private suspend fun receivedMessage(id: String, command: String) {
        // We are going to handle commands (text starting with '/') and normal messages
        when {
            // The command `who` responds the user about all the member names connected to the user.
            command.startsWith("/who") -> server.who(id)
            // The command `user` allows the user to set its name.
            command.startsWith("/user") -> {
                // We strip the command part to get the rest of the parameters.
                // In this case the only parameter is the user's newName.
                val newName = command.removePrefix("/user").trim()
                // We verify that it is a valid name (in terms of length) to prevent abusing
                when {
                    newName.isEmpty() -> server.sendTo(id, "server::help", "/user [newName]")
                    newName.length > 50 -> server.sendTo(
                        id,
                        "server::help",
                        "new name is too long: 50 characters limit"
                    )
                    else -> server.memberRenamed(id, newName)
                }
            }
            // The command 'help' allows users to get a list of available commands.
            command.startsWith("/help") -> server.help(id)
            // If no commands are matched at this point, we notify about it.
            command.startsWith("/") -> server.sendTo(
                id,
                "server::help",
                "Unknown command ${command.takeWhile { !it.isWhitespace() }}"
            )
            // Handle a normal message.
            else -> server.message(id, command)
        }
    }
}
