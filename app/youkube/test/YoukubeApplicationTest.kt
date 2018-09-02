import io.ktor.config.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.samples.youkube.*
import io.ktor.server.testing.*
import kotlinx.io.streams.*
import org.junit.Test
import java.nio.file.*
import kotlin.test.*


class YoukubeApplicationTest {
    @Test
    fun testRootWithoutVideos() = testApp {
        handleRequest(HttpMethod.Get, "/").apply {
            assertTrue { response.content!!.contains("You need to upload some videos to watch them") }
        }
    }

    @Test
    fun testUploadVideo() = testApp {
        cookiesSession {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                setBody(listOf(Login::userName.name to "root", Login::password.name to "root").formUrlEncode())
            }.apply {
                assertEquals(302, response.status()?.value)
                assertEquals(null, response.content)
                assertEquals("http://localhost/", response.headers["Location"])
            }
            handleRequest(HttpMethod.Get, "/").apply {
                assertTrue { response.content!!.contains("Upload") }
            }
            handleRequest(HttpMethod.Post, "/upload") {
                val boundary = "***bbb***"

                addHeader(HttpHeaders.ContentType, ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString())
                setBody(boundary, listOf(
                    PartData.FormItem("title123", { }, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Inline
                            .withParameter(ContentDisposition.Parameters.Name, "title")
                            .toString()
                    )),
                    PartData.FileItem({ byteArrayOf(1, 2, 3).inputStream().asInput() }, {}, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "file")
                            .withParameter(ContentDisposition.Parameters.FileName, "file.txt")
                            .toString()
                    ))
                ))
            }.apply {
                assertEquals(302, response.status()?.value)
                assertEquals("http://localhost/video/page/1", response.headers["Location"])
            }

            handleRequest(HttpMethod.Get, "/").apply {
                assertFalse { response.content!!.contains("You need to upload some videos to watch them") }
                assertTrue { response.content!!.contains("<a href=\"/video/page/1\">title123</a>") }
            }

            handleRequest(HttpMethod.Get, "/video/page/1").apply {
                assertTrue { response.content!!.contains("<video class=\"pure-u-5-5\" controls=\"controls\"><source src=\"http://localhost/video/1\" type=\"video/ogg\"></video>") }
            }
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit): Unit {
        val tempPath = Files.createTempDirectory(null).toFile().apply { deleteOnExit() }
        try {
            withTestApplication({
                (environment.config as MapApplicationConfig).apply {
                    put("youkube.session.cookie.key", "03e156f6058a13813816065")
                    put("youkube.upload.dir", tempPath.absolutePath)
                }
                main()
            }, callback)
        } finally {
            tempPath.deleteRecursively()
        }
    }
}


private class CookieTrackerTestApplicationEngine(
    val engine: TestApplicationEngine,
    var trackedCookies: List<Cookie> = listOf()
)

private fun CookieTrackerTestApplicationEngine.handleRequest(
    method: HttpMethod,
    uri: String,
    setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall {
    return engine.handleRequest(method, uri) {
        val cookieValue = trackedCookies.map { (it.name).encodeURLQueryComponent() + "=" + (it.value).encodeURLQueryComponent() }.joinToString("; ")
        addHeader("Cookie", cookieValue)
        setup()
    }.apply {
        trackedCookies = response.headers.values("Set-Cookie").map { parseServerSetCookieHeader(it) }
    }
}

private fun TestApplicationEngine.cookiesSession(
    initialCookies: List<Cookie> = listOf(),
    callback: CookieTrackerTestApplicationEngine.() -> Unit
) {
    callback(CookieTrackerTestApplicationEngine(this, initialCookies))
}
