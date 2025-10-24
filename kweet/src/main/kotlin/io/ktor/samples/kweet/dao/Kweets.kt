package io.ktor.samples.kweet.dao

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jodatime.datetime

/**
 * Represents the Kweets table using Exposed as DAO.
 */
object Kweets : Table() {
    val id = integer("id").autoIncrement()
    val user = varchar("user_id", 20).index()
    val date = datetime("date")
    val replyTo = integer("reply_to").index().nullable()
    val directReplyTo = integer("direct_reply_to").index().nullable()
    val text = varchar("text", 1024)
}
