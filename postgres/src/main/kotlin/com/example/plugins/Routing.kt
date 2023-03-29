package com.example.plugins

import com.example.exceptions.DbElementInsertException
import com.example.exceptions.DbElementNotFoundException
import com.example.models.Article
import com.example.service.ArticleService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.yaml.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*

fun Application.configureRouting(dbConnection: Connection) {
    val articleService = ArticleService(dbConnection)
    routing {
        // Create new Article
        post("/articles") {
            val article = call.receive<Article>()
            try {
                val id = articleService.create(article)
                call.respond(HttpStatusCode.Created, id)
            } catch (cause: DbElementInsertException) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        // Read an Article
        get("/articles/{id}") {
            try {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid article ID")
                val article = articleService.read(id)
                call.respond(HttpStatusCode.OK, article)
            } catch (cause: DbElementNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (cause: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        // Update an Article
        put("/articles/{id}") {
            try {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid article ID")
                val article = call.receive<Article>()
                articleService.update(id, article)
                call.respond(HttpStatusCode.OK)
            } catch (cause: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        // Delete an Article
        delete("/articles/{id}") {
            try {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid article ID")
                articleService.delete(id)
                call.respond(HttpStatusCode.OK)
            } catch (cause: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:postgresql://localhost/test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val configs = YamlConfig("postgres.yaml")
        val url = "jdbc:postgresql://localhost:5432/" +
                configs?.property("services.postgres.environment.POSTGRES_DB")?.getString()
        val user = configs?.property("services.postgres.environment.POSTGRES_USER")?.getString()
        val password = configs?.property("services.postgres.environment.POSTGRES_PASSWORD")?.getString()
        return DriverManager.getConnection(url, user, password)
    }
}