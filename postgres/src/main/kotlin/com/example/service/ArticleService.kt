package com.example.service

import com.example.exceptions.DbElementInsertException
import com.example.exceptions.DbElementNotFoundException
import com.example.models.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class ArticleService(private val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_ARTICLES =
            "CREATE TABLE IF NOT EXISTS ARTICLES (ID SERIAL PRIMARY KEY, TITLE VARCHAR(255), BODY VARCHAR(1024));"
        private const val SELECT_ARTICLE_BY_ID = "SELECT title, body FROM articles WHERE id = ?"
        private const val INSERT_ARTICLE = "INSERT INTO articles (title, body) VALUES (?, ?)"
        private const val UPDATE_ARTICLE = "UPDATE articles SET title = ?, body = ? WHERE id = ?"
        private const val DELETE_ARTICLE = "DELETE FROM articles WHERE id = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_ARTICLES)
    }

    // Create new article
    suspend fun create(article: Article): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_ARTICLE, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, article.title)
        statement.setString(2, article.body)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw DbElementInsertException("Unable to retrieve the id of the newly inserted article")
        }
    }

    // Read an article
    suspend fun read(id: Int): Article = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ARTICLE_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val title = resultSet.getString("title")
            val body = resultSet.getString("body")
            return@withContext Article(title, body)
        } else {
            throw DbElementNotFoundException("Record not found")
        }
    }

    // Update an article
    suspend fun update(id: Int, article: Article) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ARTICLE)
        statement.setString(1, article.title)
        statement.setString(2, article.body)
        statement.setInt(3, id)
        statement.executeUpdate()
    }

    // Delete an article
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ARTICLE)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}