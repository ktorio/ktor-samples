import io.ktor.application.*
import io.ktor.samples.chat.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
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
}