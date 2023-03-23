package com.example.plugins

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
            val id = articleService.create(article)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read an Article
        get("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw DbElementNotFoundException("Invalid article ID")
            try {
                val article = articleService.read(id)
                call.respond(HttpStatusCode.OK, article)
            } catch (cause: DbElementNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update an Article
        put("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw DbElementNotFoundException("Invalid article ID")
            try {
                val article = call.receive<Article>()
                articleService.update(id, article)
                call.respond(HttpStatusCode.OK)
            } catch (cause: DbElementNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Delete an Article
        delete("/articles/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw DbElementNotFoundException("Invalid article ID")
            try {
                articleService.delete(id)
                call.respond(HttpStatusCode.OK)
            } catch (cause: DbElementNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
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