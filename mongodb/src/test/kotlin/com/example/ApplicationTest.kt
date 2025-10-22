package com.example

import com.example.entities.Article
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.*
import com.example.plugins.*
import com.example.service.ArticleService
import io.ktor.client.statement.*
import io.ktor.http.*
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import org.junit.AfterClass

class ApplicationTest {
    companion object {
        private val mongoContainer = MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .apply { start() }

        private val connectionString = mongoContainer.replicaSetUrl

        @AfterClass
        @JvmStatic
        fun tearDown() {
            mongoContainer.stop()
        }
    }
    @Test
    fun testPostArticle() = testApplication {
        application {
            configureSerialization()
            val articleService = ArticleService(connectionString)
            configureRouting(articleService = articleService)
        }
        client.post("/article") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            val gson = Gson()
            setBody(gson.toJson(Article(title="Who are you?", body="Whatever you are, be a good one")))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
    }

    @Test
    fun testGetArticleId() = testApplication {
        application {
            configureSerialization()
            val articleService = ArticleService(connectionString)
            configureRouting(articleService = articleService)
        }
        val testId = getTestArticleId()
        client.get("/article/{id}") {
            parameter("id", testId)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testDeleteArticleIdDelete() = testApplication {
        application {
            configureSerialization()
            val articleService = ArticleService(connectionString)
            configureRouting(articleService = articleService)
        }
        val testId = getTestArticleId()
        client.delete("/article/{id}/delete") {
            parameter(key = "id", value = testId)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Article was deleted", bodyAsText())
        }
    }

    @Test
    fun testPutArticleIdEdit() = testApplication {
        application {
            configureSerialization()
            val articleService = ArticleService(connectionString)
            configureRouting(articleService = articleService)
        }
        client.put("/article/{id}/edit") {
            parameter(key = "id", value = getTestArticleId())
            header(HttpHeaders.Accept, ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            val gson = Gson()
            setBody(
                gson.toJson(
                    Article(
                        title="Find new opportunities",
                        body="Opportunities don't happen, you create them"
                    )
                )
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Article was edited", bodyAsText())
        }
    }

    @Test
    fun testGetArticleList() = testApplication {
        application {
            configureSerialization()
            val articleService = ArticleService(connectionString)
            configureRouting(articleService = articleService)
        }
        client.get("/article/list").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    fun getTestArticleId(): String? {
        val article = Article(title = "title", body = "body")
        val articleService = ArticleService(connectionString)
        articleService.create(article)?.let { userId ->
            return userId.toString()
        }
        return null
    }
}
