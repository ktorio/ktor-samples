package io.ktor.samples.youkube

import java.io.*

/**
 * Entity representing a [Video].
 * It includes a numeric [id], a [title] with the video title, an [authorId] with the user that uploaded the video
 * and a [videoFileName] where the video is stored.
 */
data class Video(val id: Long, val title: String, val authorId: String, val videoFileName: String) : Serializable
