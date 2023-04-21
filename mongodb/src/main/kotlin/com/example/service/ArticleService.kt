package com.example.service

import com.example.entities.Article
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId

class ArticleService {
    private val client = KMongo.createClient()
    private val database = client.getDatabase("person")
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

    fun findByTitle(title: String): List<Article> {
        val nonCaseInsensitiveFilter = Article::title regex title
        val caseInsensitiveTypeSafeFilter = (Article::title).regex(title, "i")

        val nonTypeSafeFilter = "{name:{'\$regex' : '$title', '\$options' : 'i'}}"

        val withAndOperator = articleCollection.find(
            and(Article::title regex title, (Article::body).eq("foo"))
        )

        val implicitAndOperator = articleCollection.find(
            Article::title regex title, (Article::body).eq("foo")
        )

        val withOrOperator = articleCollection.find(
            or(Article::title regex title, (Article::body).eq("foo"))
        )

        return articleCollection.find(nonTypeSafeFilter)
            .toList()
    }

    fun updateArticleByTitle(id: String, request: Article): Boolean =
        findById(id)
            ?.let { article ->
                val updateResult = articleCollection.replaceOne(article.copy(title = request.title, body = request.body))
                updateResult.modifiedCount == 1L
            } ?: false

    fun deleteArticleByTitle(id: String): Boolean {
        val deleteResult = articleCollection.deleteOne()
        return deleteResult.deletedCount == 1L
    }
}