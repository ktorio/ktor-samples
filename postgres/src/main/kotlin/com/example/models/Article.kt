package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Article(val title: String, val body: String)
