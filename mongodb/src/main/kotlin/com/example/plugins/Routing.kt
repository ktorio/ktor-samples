package com.example.plugins

import com.example.entities.*
import com.example.service.ArticleService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*

fun Application.configureRouting(articleService:ArticleService) {

    routing {

        post("/article") {
            val request = call.receive<CreateArticle>()
            val article = request.toArticle()

            articleService.create(article)?.let { userId ->
                    call.response.headers.append("My-User-Id-Header", userId.toString())
                    call.respond(HttpStatusCode.Created, userId.toString())
                } ?: call.respond(HttpStatusCode.BadRequest, ErrorResponse.BAD_REQUEST_RESPONSE)
        }

        get("/article/list") {
            val articlesList = articleService.findAll().map(Article::toDto)
            call.respond(articlesList)
        }


        get("/article/{id}") {
            val id = call.parameters["id"].toString()
            val articleById = articleService.findById(id)?.toDto()
            articleById?.let {
                call.respond(articleById)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        put("/article/{id}/edit") {
            val id = call.parameters["id"].toString()
            val article = call.receive<CreateArticle>().toArticle()
            val updatedSuccessfully = articleService.updateArticleById(id, article)
            if (updatedSuccessfully) {
                call.respond(HttpStatusCode.OK, "Article was edited")
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        delete("/article/{id}/delete") {
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
