package com.example.plugins

import com.example.entities.*
import com.example.service.ArticleService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*

fun Application.configureRouting() {
    val articleService = ArticleService()

    routing {

        post("/article") {
            val request = call.receive<ArticleDto>()
            val article = request.toArticle()

            articleService.create(article)?.let { userId ->
                    call.response.headers.append("My-User-Id-Header", userId.toString())
                    call.respond(HttpStatusCode.Created)
                } ?: call.respond(HttpStatusCode.BadRequest, ErrorResponse.BAD_REQUEST_RESPONSE)
        }

        get("/articles") {
            val articlesList = articleService.findAll().map(Article::toDto)
            call.respond(articlesList)
        }

        put("/article/edit/{id}") {
            val id = call.parameters["id"].toString()
            val article = call.receive<ArticleDto>().toArticle()
            val updatedSuccessfully = articleService.updateArticleById(id, article)
            if (updatedSuccessfully) {
                call.respond(HttpStatusCode.OK, "Article was edited")
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        delete("/article/delete/{id}") {
            val id = call.parameters["id"].toString()

            val deletedSuccessfully = articleService.deleteArticleById(id)

            if (deletedSuccessfully) {
                call.respond(HttpStatusCode.OK,"Article was deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse.NOT_FOUND_RESPONSE)
            }
        }
    }
}
