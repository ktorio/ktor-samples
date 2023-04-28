package com.example.entities

fun Article.toDto(): CreateArticle =
    CreateArticle(
        id = this.id.toString(),
        title = this.title,
        body = this.body
    )

fun CreateArticle.toArticle(): Article =
    Article(
        title = this.title,
        body = this.body
    )