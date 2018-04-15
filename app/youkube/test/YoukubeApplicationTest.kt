import io.ktor.config.*
import io.ktor.http.*
import io.ktor.samples.youkube.*
import io.ktor.server.testing.*
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
