package io.ktor.samples.httpbin

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DynamicDataTest {
    @Test
    fun base64DecodeInvalidValue() = testApplication {
        application { module() }

        val response = client.get("/base64/invalid")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("Incorrect Base64 data try: SFRUUEJJTiBpcyBhd2Vzb21l", response.bodyAsText())
    }

    @Test
    fun base64DecodeValidUrlEncodedValue() = testApplication {
        application { module() }

        val response = client.get("/base64/eyJzdWIiOiIxMjM0NTY3ODkwIn0_")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("{\"sub\":\"1234567890\"}?", response.bodyAsText())
    }

    @Test
    fun bytesInvalidNumber() = testApplication {
        application { module() }

        val response = client.get("/bytes/invalid")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun bytesNegativeNumber() = testApplication {
        application { module() }

        val response = client.get("/bytes/-10")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun bytesZeroNumber() = testApplication {
        application { module() }

        val response = client.get("/bytes/0")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/octet-stream", response.headers[HttpHeaders.ContentType])
        assertEquals(0, response.headers[HttpHeaders.ContentLength]?.toInt())
        assertEquals(0, response.body<ByteArray>().size)
    }

    @Test
    fun bytesRandomBytes() = testApplication {
        application { module() }

        val response = client.get("/bytes/20")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/octet-stream", response.headers[HttpHeaders.ContentType])
        assertEquals(20, response.headers[HttpHeaders.ContentLength]?.toInt())
        assertEquals(20, response.body<ByteArray>().size)
    }

    @Test
    fun bytesRandomWithInvalidSeed() = testApplication {
        application { module() }

        val response = client.get("/bytes/20?seed=aaa")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid seed. Expected an integer.", response.bodyAsText())
    }

    @Test
    fun bytesRandomWithSeed() = testApplication {
        application { module() }

        val response = client.get("/bytes/3?seed=42")
        assertContentEquals(byteArrayOf(-52, -17, 57), response.bodyAsBytes())
    }

    @Test
    fun bytesRandomMaxNumber() = testApplication {
        application { module() }

        val response = client.get("/bytes/9999999")
        assertEquals("application/octet-stream", response.headers[HttpHeaders.ContentType])
        assertEquals(100 * 1024, response.headers[HttpHeaders.ContentLength]?.toInt())
        assertEquals(100 * 1024, response.body<ByteArray>().size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delayGet() = virtualTimeApplication { scheduler ->
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/delay/5")
        assertEquals(5000, scheduler.currentTime)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])

        val body = response.body<JsonObject>()
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertTrue(body.has("url"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delayGetMaxDuration() = virtualTimeApplication { scheduler ->
        application { module() }

        client.get("/delay/15")
        assertEquals(10000, scheduler.currentTime)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delayGetZeroDuration() = virtualTimeApplication { scheduler ->
        application { module() }

        client.get("/delay/0")
        assertEquals(0, scheduler.currentTime)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delayGetNegativeDuration() = virtualTimeApplication { scheduler ->
        application { module() }

        client.get("/delay/-3")
        assertEquals(0, scheduler.currentTime)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delayUnsafeMethods() = virtualTimeApplication { scheduler ->
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        var start = scheduler.currentTime
        for (makeRequest in listOf(
            suspend { client.post("/delay/5") },
            suspend { client.patch("/delay/5") },
            suspend { client.put("/delay/5") },
            suspend { client.delete("/delay/5") },
        )) {
            val response = makeRequest()
            assertEquals(5000, scheduler.currentTime - start)
            start = scheduler.currentTime

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])

            val body = response.body<JsonObject>()
            assertTrue(body.has("data"))
            assertTrue(body.has("files"))
            assertTrue(body.has("form"))
            assertTrue(body.has("json"))

            assertTrue(body.has("args"))
            assertTrue(body.has("headers"))
            assertTrue(body.has("origin"))
            assertTrue(body.has("url"))
        }
    }

    @Test
    fun dripZeroDuration() = runTest {
        val flow = drip(10, 0, seqTime())

        val events = flow.toList().iterator()
        assertEquals(Bytes(10), events.next())
        assertFalse(events.hasNext())
    }

    @Test
    fun dripZeroBytes() = runTest {
        val flow = drip(0, 100, seqTime())

        val events = flow.toList().iterator()
        assertEquals(Delay(100), events.next())
        assertFalse(events.hasNext())
    }

    @Test
    fun dripPerfectDefaultCase() = runTest {
        val flow = drip(5, 1000, seqTime(
            0, 0, 200,
            200, 200, 400,
            400, 400, 600,
            600, 600, 800,
            800, 800, 1000,
            1000, 1000, 1200,
        ))

        val events = flow.toList().iterator()
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(200), events.next())
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(200), events.next())
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(200), events.next())
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(200), events.next())
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(200), events.next())
        assertFalse(events.hasNext())
    }

    @Test
    fun dripWritingTakesTime() = runTest {
        val flow = drip(3, 1000, seqTime(
            0, 100, 333,
            333, 433, 666,
            666, 766, 999,
            1005
        ))

        val events = flow.toList().iterator()
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(233), events.next())
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(233), events.next())
        assertEquals(Bytes(1), events.next())
        assertEquals(Delay(234), events.next())
        assertFalse(events.hasNext())
    }

    @Test
    fun dripDurationLessThanMinDelay() = runTest {
        val flow = drip(10, 1, seqTime(
            0, 3, 10,
        ))

        val events = flow.toList().iterator()
        assertEquals(Bytes(10), events.next())
        assertFalse(events.hasNext())
    }

    @Test
    fun dripBytesEqualsDuration() = runTest {
        val flow = drip(5, 5, seqTime(
            0, 2, 6
        ))

        val events = flow.toList().iterator()
        assertEquals(Bytes(5), events.next())
        assertEquals(Delay(3), events.next())
        assertFalse(events.hasNext())
    }

    private fun seqTime(vararg timesMs: Int): () -> Float {
        val iter = timesMs.iterator()
        return {
            iter.next().toFloat()
        }
    }

    @Test
    fun linksZero() = testApplication {
        application { module() }

        val response = client.get("/links/0/0")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/html; charset=UTF-8", response.headers["Content-Type"])
        assertEquals(
            "<html><head><title>Links</title></head><body>0 </body></html>",
            response.body()
        )
    }

    @Test
    fun linksOne() = testApplication {
        application { module() }

        val response = client.get("/links/1/0")
        assertEquals(
            "<html><head><title>Links</title></head><body>0 </body></html>",
            response.body()
        )
    }

    @Test
    fun linksTwo() = testApplication {
        application { module() }

        val response = client.get("/links/2/0")
        assertEquals(
            "<html><head><title>Links</title></head><body>0 <a href='/links/2/1'>1</a> </body></html>",
            response.body()
        )
    }

    @Test
    fun linksThree() = testApplication {
        application { module() }

        val response = client.get("/links/3/0")
        assertEquals(
            "<html><head><title>Links</title></head><body>0 " +
                    "<a href='/links/3/1'>1</a> " +
                    "<a href='/links/3/2'>2</a> " +
                    "</body></html>",
            response.body()
        )
    }

    @Test
    fun linksNonZeroOffset() = testApplication {
        application { module() }

        val response = client.get("/links/3/2")
        assertEquals(
            "<html><head><title>Links</title></head><body>" +
                    "<a href='/links/3/0'>0</a> <a href='/links/3/1'>1</a> 2 " +
                    "</body></html>",
            response.body()
        )
    }

    @Test
    fun linksMinNumber() = testApplication {
        application { module() }

        val response = client.get("/links/0/2")
        assertEquals(
            "<html><head><title>Links</title></head><body>" +
                    "<a href='/links/1/0'>0</a> " +
                    "</body></html>",
            response.body()
        )
    }

    @Test
    fun rangeInvalidSize() = testApplication {
        application { module() }

        val response = client.get("/range/0")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("bytes", response.headers[HttpHeaders.AcceptRanges])
        assertEquals("text/html; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertEquals("range0", response.headers[HttpHeaders.ETag])
        assertEquals("number of bytes must be in the range (0, 102400]", response.body())
    }

    @Test
    fun rangeNoRange() = testApplication {
        application { module() }

        val response = client.get("/range/10")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/octet-stream", response.headers[HttpHeaders.ContentType])
        assertEquals("abcdefghij", response.bodyAsText())
    }

    @Test
    fun rangeAtStart() = testApplication {
        application { module() }

        val response = client.get("/range/100") {
            header(HttpHeaders.Range, "bytes=0-25")
        }

        assertEquals("range100", response.headers[HttpHeaders.ETag])
        assertEquals("bytes 0-25/100", response.headers[HttpHeaders.ContentRange])
        assertEquals(('a'..'z').joinToString(separator = ""), response.bodyAsText())
    }

    @Test
    fun rangeInMiddle() = testApplication {
        application { module() }

        val response = client.get("/range/100") {
            header(HttpHeaders.Range, "bytes=33-43")
        }

        assertEquals("range100", response.headers[HttpHeaders.ETag])
        assertEquals("bytes 33-43/100", response.headers[HttpHeaders.ContentRange])
        assertEquals("hijklmnopqr", response.bodyAsText())
    }

    @Test
    fun rangeInEnd() = testApplication {
        application { module() }

        val response = client.get("/range/100") {
            header(HttpHeaders.Range, "bytes=90-100")
        }

        assertEquals("mnopqrstuv", response.bodyAsText())
    }

    @Test
    fun rangeNonSatisfied() = testApplication {
        application { module() }

        val response = client.get("/range/100") {
            header(HttpHeaders.Range, "bytes=102-110")
        }

        assertEquals(HttpStatusCode.RequestedRangeNotSatisfiable, response.status)
    }

    @Test
    fun streamBytesZero() = testApplication {
        application { module() }

        val response = client.get("/stream-bytes/0")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/octet-stream", response.headers[HttpHeaders.ContentType])
        assertEquals(0, response.bodyAsBytes().size)
    }

    @Test
    fun streamFewBytes() = testApplication {
        application { module() }

        val response = client.get("/stream-bytes/100")
        assertEquals(100, response.bodyAsBytes().size)
    }

    @Test
    fun streamBytesSeed() = testApplication {
        application { module() }

        val response = client.get("/stream-bytes/3?seed=42")
        assertContentEquals(byteArrayOf(-52, -17, 57), response.bodyAsBytes())
    }

    @Test
    fun streamBytesChuckSize() = testApplication {
        application { module() }

        val response = client.get("/stream-bytes/10000?chunk-size=333")
        assertEquals(10_000, response.bodyAsBytes().size)
    }

    @Test
    fun streamDifferentChunkSizesSameStream() = testApplication {
        application { module() }

        val bodySmaller = client.get("/stream-bytes/10000?seed=42&chunk_size=333").bodyAsBytes()
        val bodyLarger = client.get("/stream-bytes/10000?seed=42&chunk_size=1000").bodyAsBytes()
        assertContentEquals(bodySmaller, bodyLarger)
    }

    @Test
    fun streamZeroJsons() = testApplication {
        application { module() }

        val response = client.get("/stream/0")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json", response.headers[HttpHeaders.ContentType])
        assertEquals(0, response.bodyAsBytes().size)
    }

    @Test
    fun streamOneJson() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.get("/stream/1").body<JsonObject>()
        assertTrue(body.has("url"))
        assertTrue(body.has("args"))
        assertTrue(body.has("headers"))
        assertTrue(body.has("origin"))
        assertEquals(0, body.get("id").asInt)
    }

    @Test
    fun streamFewJsons() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val jsons = client.get("/stream/3").bodyAsText().lines().iterator()

        assertEquals(
            0,
            Gson().fromJson(jsons.next(), JsonObject::class.java).get("id").asInt
        )
        assertEquals(
            1,
            Gson().fromJson(jsons.next(), JsonObject::class.java).get("id").asInt
        )
        assertEquals(
            2,
            Gson().fromJson(jsons.next(), JsonObject::class.java).get("id").asInt
        )

        assertEquals("", jsons.next()) // Terminating new line
        assertFalse(jsons.hasNext())
    }

    @Test
    fun uuid() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val response = client.get("/uuid")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers[HttpHeaders.ContentType])
        assertNotNull(response.headers[HttpHeaders.ContentLength])
        val body = response.body<JsonObject>()

        assertTrue(""".{8}-.{4}-.{4}-.{4}-.{12}""".toRegex().matches(body["uuid"].asString))
    }

    private fun virtualTimeApplication(
        block: suspend ApplicationTestBuilder.(
            scheduler: TestCoroutineScheduler
        ) -> Unit
    ) {
        val scheduler = TestCoroutineScheduler()
        val disp = StandardTestDispatcher(scheduler)

        runTest(disp) {
            runTestApplication(coroutineContext) {
                engine {
                    dispatcher = disp
                }

                block(scheduler)
            }
        }
    }
}