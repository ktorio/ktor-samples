package io.ktor.samples.httpbin

import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnsafeMethodsBodyTest {
    @Test
    fun noBody() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post").body<JsonObject>()

        assertTrue(body.has("data"))
        assertEquals("", body.get("data").asString)

        assertTrue(body.has("files"))
        assertTrue(body.get("files").asJsonObject.isEmpty)

        assertTrue(body.has("form"))
        assertTrue(body.get("form").asJsonObject.isEmpty)

        assertTrue(body.has("json"))
        assertTrue(body.get("json").isJsonNull)
    }

    @Test
    fun keyAreSorted() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post").body<JsonObject>()

        val keys = body.asJsonObject.keySet().iterator()

        assertEquals("args", keys.next())
        assertEquals("data", keys.next())
        assertEquals("files", keys.next())
        assertEquals("form", keys.next())
        assertEquals("headers", keys.next())
        assertEquals("json", keys.next())
        assertEquals("origin", keys.next())
        assertEquals("url", keys.next())
    }

    @Test
    fun textBody() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("text body")
        }.body<JsonObject>()

        assertEquals("text body", body.get("data").asString)
    }

    private val binaryPayload = byteArrayOf(
        0xC0.toByte(), 0xAF.toByte(), 0xF5.toByte(), 0xFF.toByte(),
        0xFE.toByte(), 0xC1.toByte(), 0x80.toByte(), 0xED.toByte(),
        0xA0.toByte(), 0x80.toByte(), 0xED.toByte(), 0xBF.toByte(),
        0xBF.toByte(), 0xF4.toByte(), 0x90.toByte(), 0x80.toByte(),
        0x80.toByte(), 0x80.toByte(), 0xFF.toByte()
    )

    @Test
    fun binaryBodyTextPlainContentType() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(binaryPayload)
            header("Content-Type", "text/plain")
        }.body<JsonObject>()

        assertEquals(
            "data:application/octet-stream;base64,wK/1//7BgO2ggO2/v/SQgICA/w==",
            body.get("data").asString
        )
    }

    @Test
    fun binaryBodyOtherContentType() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(binaryPayload)
            header("Content-Type", "text/xml")
        }.body<JsonObject>()

        assertEquals(
            "data:application/octet-stream;base64,wK/1//7BgO2ggO2/v/SQgICA/w==",
            body.get("data").asString
        )
    }

    @Test
    fun nakedParamUrlEncoded() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("param")
            contentType(ContentType.Application.FormUrlEncoded)
        }.body<JsonObject>()

        val form = body.get("form").asJsonObject
        assertEquals("", form.get("param").asString)

        assertEquals("", body.get("data").asString)
    }

    @Test
    fun paramUrlEncoded() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("param=value")
            contentType(ContentType.Application.FormUrlEncoded)
        }.body<JsonObject>()

        val form = body.get("form").asJsonObject
        assertEquals("value", form.get("param").asString)
    }

    @Test
    fun multipleParamsUrlEncoded() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("p1=1&p2&p3")
            contentType(ContentType.Application.FormUrlEncoded)
        }.body<JsonObject>()

        val form = body.get("form").asJsonObject
        assertEquals("1", form.get("p1").asString)
        assertEquals("", form.get("p2").asString)
        assertEquals("", form.get("p3").asString)
    }

    @Test
    fun paramsAreSortedUrlEncoded() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("p3&p1=1&p2")
            contentType(ContentType.Application.FormUrlEncoded)
        }.body<JsonObject>()

        val form = body.get("form").asJsonObject
        val keys = form.keySet().iterator()

        assertEquals("p1", keys.next())
        assertEquals("p2", keys.next())
        assertEquals("p3", keys.next())
        assertFalse(keys.hasNext())
    }

    @Test
    fun simpleJsonValue() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("123")
        }.body<JsonObject>()

        assertEquals(123, body.get("json").asJsonPrimitive.asInt)
    }

    @Test
    fun jsonObject() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody("""
                {"name": {}, "values": [1, 2, 3], "flag": true, "nothing": null} 
            """.trimIndent())
        }.body<JsonObject>()

        val json = body.get("json").asJsonObject
        assertTrue(json.get("name").asJsonObject.isEmpty)

        val values = json.get("values").asJsonArray.iterator()
        assertEquals(1, values.next().asJsonPrimitive.asInt)
        assertEquals(2, values.next().asJsonPrimitive.asInt)
        assertEquals(3, values.next().asJsonPrimitive.asInt)

        assertEquals(true, json.get("flag").asJsonPrimitive.asBoolean)
        assertTrue(json.get("nothing").isJsonNull)
    }

    @Test
    fun multipartSimpleParam() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("param", "value")
                },
                "MyBoundary"
            ))
        }.body<JsonObject>()

        assertEquals(
            "multipart/form-data; boundary=MyBoundary",
            body.get("headers").asJsonObject.get("Content-Type").asString
        )

        val form = body.get("form").asJsonObject
        assertEquals("value", form.get("param").asString)
    }

    @Test
    fun multipartSingleTextFile() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("text", "text data", Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=file")
                    })
                },
            ))
        }.body<JsonObject>()

        assertTrue(body.get("form").asJsonObject.isEmpty)

        val files = body.get("files").asJsonObject
        assertEquals("text data", files.get("text").asString)
    }

    @Test
    fun multipartBinaryFile() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("binary", binaryPayload, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=file")
                    })
                },
            ))
        }.body<JsonObject>()

        val files = body.get("files").asJsonObject
        assertEquals(
            "data:application/octet-stream;base64,wK/1//7BgO2ggO2/v/SQgICA/w==",
            files.get("binary").asString
        )
    }

    @Test
    fun multipartSpecialCharsInFile() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("data", "\n\t\r\b\\", Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=file")
                    })
                },
            ))
        }.body<JsonObject>()

        val files = body.get("files").asJsonObject
        assertEquals("\n\t\r\b\\", files.get("data").asString)
    }

    @Test
    fun multipartFilesAreSorted() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                gson()
            }
        }

        val body = client.post("/post") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("p2", "test2", Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=file1")
                    })
                    append("p3", "tes3", Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=file2")
                    })
                    append("p1", "test1", Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=file3")
                    })
                },
            ))
        }.body<JsonObject>()

        val files = body.get("files").asJsonObject.keySet().iterator()
        assertEquals("p1", files.next())
        assertEquals("p2", files.next())
        assertEquals("p3", files.next())
    }
}