package io.ktor.samples.plugins

import io.ktor.samples.*
import io.ktor.server.application.*
import io.ktor.server.http.*
import java.sql.ResultSet
import java.time.Instant

data class Message(val text: String, val date: Instant) {
    override fun toString(): String = "${date.toHttpDateString()}: $text"
}

class MessageBoard(application: Application) {
    private val db = application.h2()

    init {
        initSchema()

        application.environment.monitor.subscribe(ApplicationStopping) {
            close()
        }
    }

    fun list(): List<Message> {
        db.connection.use {
            it.prepareStatement("SELECT * FROM messages").use { stmt ->
                stmt.executeQuery().use { rs ->
                    return rs.readMessages()
                }
            }
        }
    }

    fun post(text: String) {
        db.connection.use {
            it.prepareStatement("INSERT INTO messages (text, date) VALUES (?, ?)")
                .use { stmt ->
                    stmt.setString(1, text)
                    stmt.setTimestamp(2, java.sql.Timestamp.from(Instant.now()))
                    stmt.execute()
                }
        }
    }

    private fun initSchema() {
        db.connection.use {
            it.prepareStatement(
                """
CREATE TABLE IF NOT EXISTS messages
(
    id        INTEGER auto_increment,
    text      VARCHAR(255) NOT NULL,
    date      TIMESTAMP    NOT NULL
)
                """.trimIndent()
            ).execute()
        }
    }

    private fun close() {
        db.close()
    }
}

private fun ResultSet.readMessages(): List<Message> {
    val messages = mutableListOf<Message>()

    while (next()) {
        messages.add(
            Message(
                text = getString("text"),
                date = getTimestamp("date").toInstant()
            )
        )
    }

    return messages
}
