package com.example.entities

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

data class Article(
    @BsonId
    val id: Id<Article>? = null,
    val title: String,
    val body: String
)
