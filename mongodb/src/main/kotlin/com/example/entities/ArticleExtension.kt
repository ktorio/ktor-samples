package com.example.entities

fun Article.toDto(): ArticleDto =
    ArticleDto(
        id = this.id.toString(),
        title = this.title,
        body = this.body
    )

fun ArticleDto.toArticle(): Article =
    Article(
        title = this.title,
        body = this.body
    )