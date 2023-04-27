package com.example.service

import com.example.entities.Article
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId

class ArticleService {
    private val client = KMongo.createClient()
    private val database = client.getDatabase("article")
    private val articleCollection = database.getCollection<Article>()

    fun create(article: Article): Id<Article>? {
        articleCollection.insertOne(article)
        return article.id
    }

    fun findAll(): List<Article> =
        articleCollection.find()
            .toList()

    fun findById(id: String): Article? {
        val bsonId: Id<Article> = ObjectId(id).toId()
        return articleCollection
            .findOne(Article::id eq bsonId)
    }

    fun updateArticleById(id: String, request: Article): Boolean =
        findById(id)
            ?.let { article ->
                val updateResult =
                    articleCollection.replaceOne(article.copy(title = request.title, body = request.body))
                updateResult.modifiedCount == 1L
            } ?: false

    fun deleteArticleById(id: String): Boolean {
        val bsonId: Id<Article> = ObjectId(id).toId()
        val deleteResult = articleCollection.deleteOneById(bsonId)
        return deleteResult.deletedCount == 1L
    }

    fun release() {
        client.close()
    }
}