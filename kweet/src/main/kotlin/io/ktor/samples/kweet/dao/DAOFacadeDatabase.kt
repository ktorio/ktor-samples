package io.ktor.samples.kweet.dao

import io.ktor.samples.kweet.dao.Kweets.id
import io.ktor.samples.kweet.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.*
import org.joda.time.*
import java.io.*

/**
 * A DAO Facade interface for the Database. This allows us to provide several implementations.
 *
 * In this case, this is used to provide a Database-based implementation using Exposed,
 * and a cache implementation composing another DAOFacade.
 */
interface DAOFacade : Closeable {
    /**
     * Initializes all the required data.
     * In this case, this should initialize the Users and Kweets tables.
     */
    fun init()

    /**
     * Counts the number of replies of a kweet identified by its [id].
     */
    fun countReplies(id: Int): Int

    /**
     * Creates a Kweet from a specific [user] name, the kweet [text] content,
     * an optional [replyTo] id of the parent kweet, and a [date] that would default to the current time.
     */
    fun createKweet(user: String, text: String, replyTo: Int? = null, date: DateTime = DateTime.now()): Int

    /**
     * Deletes a kweet from its [id].
     */
    fun deleteKweet(id: Int)

    /**
     * Get the DAO object representation of a kweet based from its [id].
     */
    fun getKweet(id: Int): Kweet

    /**
     * Obtains a list of integral ids of kweets from a specific user identified by its [userId].
     */
    fun userKweets(userId: String): List<Int>

    /**
     * Tries to get a user from its [userId] and optionally its password [hash].
     * If the [hash] is specified, the password [hash] must match, or the function will return null.
     * If no [hash] is specified, it will return the [User] if exists, or null otherwise.
     */
    fun user(userId: String, hash: String? = null): User?

    /**
     * Tries to get a user from its [email].
     *
     * Returns null if no user has this [email] associated.
     */
    fun userByEmail(email: String): User?

    /**
     * Creates a new [user] in the database from its object [User] representation.
     */
    fun createUser(user: User)

    /**
     * Returns a list of Kweet ids, with the ones with most replies first.
     */
    fun top(count: Int = 10): List<Int>

    /**
     * Returns a list of Keet ids, with the recent ones first.
     */
    fun latest(count: Int = 10): List<Int>
}

/**
 * Database implementation of the facade.
 * Uses Exposed, and either an in-memory H2 database or a file-based H2 database by default.
 * But it can be configured.
 */
class DAOFacadeDatabase(
    val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
) : DAOFacade {
    constructor(dir: File) : this(
        Database.connect(
            "jdbc:h2:file:${dir.canonicalFile.absolutePath}",
            driver = "org.h2.Driver"
        )
    )

    override fun init() {
        // Create the used tables
        transaction(db) {
            SchemaUtils.create(Users, Kweets)
        }
    }

    override fun countReplies(id: Int): Int = transaction(db) {
        (Kweets.slice(Kweets.id.count()).select {
            Kweets.replyTo.eq(id)
        }.single()[Kweets.id.count()]).toInt()
    }

    override fun createKweet(user: String, text: String, replyTo: Int?, date: DateTime): Int = transaction(db) {
        Kweets.insert {
            it[Kweets.user] = user
            it[Kweets.date] = date
            it[Kweets.replyTo] = replyTo
            it[Kweets.text] = text
        }.resultedValues?.firstOrNull()?.get(Kweets.id) ?: error("No generated key returned")
    }

    override fun deleteKweet(id: Int) {
        transaction(db) {
            Kweets.deleteWhere { Kweets.id.eq(id) }
        }
    }

    override fun getKweet(id: Int) = transaction(db) {
        val row = Kweets.select { Kweets.id.eq(id) }.single()
        Kweet(id, row[Kweets.user], row[Kweets.text], row[Kweets.date], row[Kweets.replyTo])
    }

    override fun userKweets(userId: String) = transaction(db) {
        Kweets.slice(Kweets.id).select { Kweets.user.eq(userId) }.orderBy(Kweets.date, SortOrder.DESC).limit(100)
            .map { it[Kweets.id] }
    }

    override fun user(userId: String, hash: String?) = transaction(db) {
        Users.select { Users.id.eq(userId) }
            .mapNotNull {
                if (hash == null || it[Users.passwordHash] == hash) {
                    User(userId, it[Users.email], it[Users.displayName], it[Users.passwordHash])
                } else {
                    null
                }
            }
            .singleOrNull()
    }

    override fun userByEmail(email: String) = transaction(db) {
        Users.select { Users.email.eq(email) }
            .map { User(it[Users.id], email, it[Users.displayName], it[Users.passwordHash]) }.singleOrNull()
    }

    override fun createUser(user: User) = transaction(db) {
        Users.insert {
            it[id] = user.userId
            it[displayName] = user.displayName
            it[email] = user.email
            it[passwordHash] = user.passwordHash
        }
        Unit
    }

    override fun top(count: Int): List<Int> = transaction(db) {
        // note: In a real application, you shouldn't do it like this
        //   as it may cause database outages on big data
        //   so this implementation is just for demo purposes

        val k2 = Kweets.alias("k2")
        Kweets.join(k2, JoinType.LEFT, Kweets.id, k2[Kweets.replyTo])
            .slice(Kweets.id, k2[Kweets.id].count())
            .selectAll()
            .groupBy(Kweets.id)
            .orderBy(k2[Kweets.id].count(), SortOrder.DESC)
//                .having { k2[Kweets.id].count().greater(0) }
            .limit(count)
            .map { it[Kweets.id] }
    }

    override fun latest(count: Int): List<Int> = transaction(db) {
        var attempt = 0
        var allCount: Int? = null

        for (minutes in generateSequence(2) { it * it }) {
            attempt++

            val dt = DateTime.now().minusMinutes(minutes)

            val all = Kweets.slice(Kweets.id)
                .select { Kweets.date.greater(dt) }
                .orderBy(Kweets.date, SortOrder.DESC)
                .limit(count)
                .map { it[Kweets.id] }

            if (all.size >= count) {
                return@transaction all
            }
            if (attempt > 10 && allCount == null) {
                allCount = Kweets.slice(Kweets.id.count()).selectAll().count().toInt()
                if (allCount <= count) {
                    return@transaction Kweets.slice(Kweets.id).selectAll().map { it[Kweets.id] }
                }
            }
        }

        emptyList()
    }

    override fun close() {
    }
}
