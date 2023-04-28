package com.example.entities

import kotlinx.serialization.Serializable

@Serializable
data class CreateArticle(
    val id: String? = null,
    val title: String,
    val body: String)
