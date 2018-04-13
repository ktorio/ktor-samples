package io.ktor.samples.kweet

import io.ktor.samples.kweet.dao.*
import io.ktor.samples.kweet.model.*
import io.ktor.server.testing.*
import org.joda.time.*
import org.junit.*

class KweetApplicationTest {
    @Test
    fun name() {
        withTestApplication {

        }
    }

    class TestDAOFacade : DAOFacade {
        override fun init() = Unit
        override fun countReplies(id: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun createKweet(user: String, text: String, replyTo: Int?, date: DateTime): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun deleteKweet(id: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getKweet(id: Int): Kweet {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun userKweets(userId: String): List<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun user(userId: String, hash: String?): User? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun userByEmail(email: String): User? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun createUser(user: User) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun top(count: Int): List<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun latest(count: Int): List<Int> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun close() = Unit
    }
}