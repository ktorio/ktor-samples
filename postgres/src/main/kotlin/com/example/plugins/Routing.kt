package com.example.plugins

import com.example.models.Article
import com.example.service.ArticleService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.sql.*

fun Application.configureRouting() {
    val dbConnection: Connection = connectToPostgres(embedded = false)
    val articleService = ArticleService(dbConnection)
    routing {
        // Create new Article
        post("/articles") {
            val article = call.receive<Article>()
            val id = articleService.create(article)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read an Article
        get("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val article = articleService.read(id)
                call.respond(HttpStatusCode.OK, article)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update an Article
        put("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val article = call.receive<Article>()
            articleService.update(id, article)
            call.respond(HttpStatusCode.OK)
        }
        // Delete an Article
        delete("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            articleService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:postgresql://localhost/test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}