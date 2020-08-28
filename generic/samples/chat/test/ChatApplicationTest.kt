import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.samples.chat.*
import io.ktor.server.testing.*
import kotlin.test.*

/**
 * Tests the [ChatApplication].
 */
class ChatApplicationTest {
    /**
     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
     */
    @Test
    fun testSimpleConversation() {
        // First we create a [TestApplicationEngine] that includes the module [Application.main],
        // this executes that function and thus installs all the features and routes to this test application.
        withTestApplication(Application::main) {
            // Keeps a log array that will hold all the events we want to check later at once.
            val log = arrayListOf<String>()

            // We perform a test websocket connection to this route. Effectively acting as a client.
            // The [incoming] parameter allows to receive frames, while the [outgoing] allows to send frames to the server.
            handleWebSocketConversation("/ws") { incoming, outgoing ->
                // Send a HELLO message
                outgoing.send(Frame.Text("HELLO"))

                // We then receive two messages (the message notifying that the member joined, and the message we sent echoed to us)
                for (n in 0 until 2) {
                    log += (incoming.receive() as Frame.Text).readText()
                }
            }

            // Verify the received messages.
            assertEquals(
                listOf(
                    "[server] Member joined: user1.",
                    "[user1] HELLO"
                ), log
            )
        }
    }

    /**
     * This is an integration test that verifies the behaviour with two connected clients.
     *
     * NOTE: to prevent repeating, check the previous test for detailed explanation of what does what.
     */
    @Test
    fun testDualConversation() {
        // Creates the [TestApplicationEngine] with the [Application::main] module. Check the previous test for more details.
        withTestApplication(Application::main) {
            // Sets to hold the messages from each children.
            // Since this is multithreaded and socket-related.
            // The order might change in each run, so we use a Set instead of a List to check that the messages
            // are received without having into account exact the order.
            val log1 = hashSetOf<String>()
            val log2 = hashSetOf<String>()

            // Perform a test connection to this route (client1): incoming1, outgoing1
            handleWebSocketConversation("/ws") { incoming1, outgoing1 ->
                // We log the `Member joined` for this user.
                log1 += (incoming1.receive() as Frame.Text).readText()

                // Perform a test connection to this route (client2): incoming2, outgoing2
                handleWebSocketConversation("/ws") { incoming2, outgoing2 ->
                    // A Member joined: user2 happens here
                    outgoing1.send(Frame.Text("HELLO")) // Client1 says HELLO
                    outgoing2.send(Frame.Text("HI")) // Client2 says HI

                    // Both clients now have three messages to read (Member joined + HELLO + HI)
                    for (n in 0 until 3) log1 += (incoming1.receive() as Frame.Text).readText()
                    for (n in 0 until 3) log2 += (incoming2.receive() as Frame.Text).readText()
                }
            }

            // Checks what USER1 received
            assertEquals(
                setOf(
                    "[server] Member joined: user1.",
                    "[server] Member joined: user2.",
                    "[user1] HELLO",
                    "[user2] HI"
                ), log1
            )

            // Checks what USER2 received
            assertEquals(
                setOf(
                    "[server] Member joined: user2.",
                    "[user1] HELLO",
                    "[user2] HI"
                ), log2
            )
        }
    }
}