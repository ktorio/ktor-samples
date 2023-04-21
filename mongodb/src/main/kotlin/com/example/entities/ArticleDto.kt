package com.example.entities

import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
    val id: String? = null,
    val title: String,
    val body: String)
