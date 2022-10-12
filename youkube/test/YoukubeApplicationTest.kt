import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.samples.youkube.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import java.io.*
import kotlin.test.*

/**
 * Integration tests for the [main] module.
 */
class YoukubeApplicationTest {
    /**
     * Verifies that the [Index] page, returns content with "You need to upload some videos to watch them"
     * for an empty test application.
     */
    @Test
    fun testRootWithoutVideos() = testApplication {
        client.get("/").apply {
            assertTrue { bodyAsText().contains("You need to upload some videos to watch them") }
        }
    }
    /**
     * Verifies the complete process of [Login] with the valid credentials (root:root) for this application,
     * obtains the [Index] verifying that it now offers [Upload]ing files and that it links to the newly created [Video].
     * Then it tries to access the [VideoPage] and ensures that it has a [kotlinx.html.VIDEO] element with the video.
     */
    @Test
    fun testUploadVideo() = testApplication {
        val uploadDir = File(".youkube-video")
        val client = createClient {
            install(HttpCookies)
        }
        client.get("/").apply {
            assertTrue { bodyAsText().contains("You need to upload some videos to watch them") }
        }
        client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf(Login::userName.name to "root", Login::password.name to "root").formUrlEncode())
        }.apply {
            assertEquals(302, status.value)
        }
        client.get("/").apply {
            assertTrue { bodyAsText().contains("Upload") }
        }
        client.post("/upload") {
            val boundary = "***bbb***"
            setBody(
                MultiPartFormDataContent(
                    listOf(
                        PartData.FormItem(
                            "title123", { },
                            headersOf(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Inline
                                    .withParameter(ContentDisposition.Parameters.Name, "title")
                                    .toString()
                            )
                        ),
                        PartData.FileItem(
                            { byteArrayOf(1, 2, 3).inputStream().asInput() }, {},
                            headersOf(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.File
                                    .withParameter(ContentDisposition.Parameters.Name, "file")
                                    .withParameter(ContentDisposition.Parameters.FileName, "file.txt")
                                    .toString()
                            )
                        )
                    ),
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }.apply {
            assertEquals(302, status.value)
            assertEquals("/video/page/1", headers["Location"])
        }
        client.get("/").apply {
            assertFalse { bodyAsText().contains("You need to upload some videos to watch them") }
            assertTrue { bodyAsText().contains("<a href=\"/video/page/1\">title123</a>") }
        }
        client.get("/video/page/1").apply {
            assertTrue { bodyAsText().contains("<video class=\"pure-u-5-5\" controls=\"controls\"><source src=\"/video/1\" type=\"video/ogg\"></video>") }
        }
        uploadDir.deleteRecursively()
    }
}
