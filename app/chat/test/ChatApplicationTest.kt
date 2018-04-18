import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.samples.chat.*
import io.ktor.server.testing.*
import kotlin.test.*

class ChatApplicationTest {
    @Test
    fun testSimpleConversation() {
        withTestApplication(Application::main) {
            val log = arrayListOf<String>()
            handleWebSocketConversation("/ws") { incoming, outgoing ->
                outgoing.send(Frame.Text("HELLO"))
                for (n in 0 until 2) {
                    log += (incoming.receive() as Frame.Text).readText()
                }
            }
            assertEquals(
                listOf(
                    "[server] Member joined: user1.",
                    "[user1] HELLO"
                ), log
            )
        }
    }

    @Test
    fun testDualConversation() {
        withTestApplication(Application::main) {
            val log1 = hashSetOf<String>()
            val log2 = hashSetOf<String>()
            handleWebSocketConversation("/ws") { incoming1, outgoing1 ->
                log1 += (incoming1.receive() as Frame.Text).readText()
                handleWebSocketConversation("/ws") { incoming2, outgoing2 ->
                    outgoing1.send(Frame.Text("HELLO"))
                    outgoing2.send(Frame.Text("HI"))
                    for (n in 0 until 3) log1 += (incoming1.receive() as Frame.Text).readText()
                    for (n in 0 until 3) log2 += (incoming2.receive() as Frame.Text).readText()
                }
            }
            assertEquals(
                setOf(
                    "[server] Member joined: user1.",
                    "[server] Member joined: user2.",
                    "[user1] HELLO",
                    "[user2] HI"
                ), log1
            )
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