package io.ktor.samples.youkube

import com.google.gson.*
import org.ehcache.*
import org.ehcache.config.*
import java.io.*
import java.util.*
import java.util.concurrent.atomic.*

/**
 * Class that represents [Database] of the application.
 * It uses a folder instead of a real database to store videos an indexes,
 */
class Database(val uploadDir: File) {
    /**
     * A [GsonBuilder] used for storing the video information in a `.idx` file.
     */
    val gson = GsonBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create()

    /**
     * Creates a ehcache used for caching.
     */
    val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)

    /**
     * Ehcache used for caching the metadata of the videos.
     */
    @Suppress("UNCHECKED_CAST")
    val videosCache = cacheManager.createCache<Long, Video>("videos",
            CacheConfigurationBuilder.newCacheConfigurationBuilder<Long, Video>().buildConfig(Class.forName("java.lang.Long") as Class<Long>, Video::class.java))

    private val digitsOnlyRegex = "\\d+".toRegex()
    private val allIds by lazy {
        uploadDir.listFiles { f -> f.extension == "idx" && f.nameWithoutExtension.matches(digitsOnlyRegex) }.mapTo(ArrayList()) { it.nameWithoutExtension.toLong() }
    }

    /**
     * Stores the last id of this database to provide incremental unique ids.
     */
    val biggestId by lazy { AtomicLong(allIds.max() ?: 0) }

    /**
     * Returns a [Sequence] with all te [Video]s.
     */
    fun listAll(): Sequence<Video> = allIds.asSequence().mapNotNull { videoById(it) }

    /**
     * Returns the first 10 uploaded videos.
     */
    fun top() = listAll().take(10).toList()

    /**
     * Tries to obtain a [Video] from its numeric [id].
     *
     * First tries to search in the cache, and if not available,
     * tries to read it from a file inside the [uploadDir] holding the video metadata.
     */
    fun videoById(id: Long): Video? {
        val video = videosCache.get(id)
        if (video != null) {
            return video
        }

        try {
            val json = gson.fromJson(File(uploadDir, "$id.idx").readText(), Video::class.java)
            videosCache.put(id, json)

            return json
        } catch (e: Throwable) {
            return null
        }
    }

    /**
     * Computes a unique incremental numeric ID for representing a new video.
     */
    fun nextId() = biggestId.incrementAndGet()

    /**
     * Creates a [Video] metadata information with a new unique id, and stores it in disk and the cache.
     */
    fun addVideo(title: String, userId: String, file: File): Long {
        val id = nextId()
        val video = Video(id, title, userId, file.path)

        File(uploadDir, "$id.idx").writeText(gson.toJson(video))
        allIds.add(id)

        videosCache.put(id, video)

        return id
    }
}