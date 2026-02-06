package io.ktor.samples.httpbin

import io.ktor.openapi.GenericElement
import io.ktor.openapi.JsonSchema
import io.ktor.openapi.ReferenceOr
import io.ktor.openapi.Response
import io.ktor.openapi.jsonSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

fun Response.Builder.schemaGet(): JsonSchema {
    return partialSchema<HttpbinResponse>(
        "HttpbinGet",
        "args", "headers", "origin", "url"
    )
}

fun Response.Builder.schemaUnsafe(): JsonSchema {
    return partialSchema<HttpbinResponse>(
        "HttpbinUnsafe",
        "args", "data", "files", "form", "headers", "json", "origin", "url"
    )
}

val ALL_EXAMPLES = mapOf(
    "args" to GenericElement(
        mapOf(
            "param" to GenericElement("value"),
            "list" to GenericElement(listOf("v1", "v2")),
        )
    ),
    "headers" to GenericElement(
        mapOf(
            "Host" to "example.com",
            "Accept" to "*/*",
        )
    ),
    "origin" to GenericElement("127.0.0.1"),
    "url" to GenericElement("https://example.com/"),
    "files" to GenericElement(
        mapOf(
            "file1" to GenericElement("text data"),
            "file2" to GenericElement("data:application/octet-stream;base64,wK/1//7BgO2ggO2/v/SQgICA/w==")
        )
    ),
    "form" to GenericElement(
        mapOf(
            "field" to GenericElement("value"),
            "multi-field" to GenericElement(listOf("v1", "v2")),
        )
    ),
    "data" to GenericElement("text data"),
    "json" to GenericElement(
        JsonObject(
            mapOf(
                "key" to JsonPrimitive("value"),
                "array" to JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2))),
            )
        )
    ),
    "user" to GenericElement("john.doe"),
    "authenticated" to GenericElement(true),
    "token" to GenericElement("demo_4f2c9a1b8d7e4c3a9f12b0e6d1a2c3f4"),
    "user-agent" to GenericElement("Mozilla/5.0"),
    "brotli" to GenericElement(true),
    "deflated" to GenericElement(true),
    "gzipped" to GenericElement(true),
    "uuid" to GenericElement("d66f7c52-ca29-4846-a848-656aae064b1d"),
    "cookies" to GenericElement(mapOf(
        "cookie1" to "value1",
        "cookie2" to "value2"
    )),
)

inline fun <reified T : Any> Response.Builder.partialSchema(
    title: String,
    vararg propNames: String,
): JsonSchema {
    val fullSchema = jsonSchema<T>()

    val props = mutableMapOf<String, ReferenceOr<JsonSchema>>()
    for ((p, v) in fullSchema.properties ?: emptyMap()) {
        if (p in propNames) {
            if (ALL_EXAMPLES.containsKey(p)) {
                props[p] = v.mapValue { it.copy(example = ALL_EXAMPLES[p]) }
            } else {
                props[p] = v
            }
        }
    }

    return fullSchema.copy(
        title = title,
        properties = props,
    )
}

inline fun <reified T : Any> Response.Builder.schemaWithExamples(title: String): JsonSchema {
    val fullSchema = jsonSchema<T>()

    val props = mutableMapOf<String, ReferenceOr<JsonSchema>>()
    for ((p, v) in fullSchema.properties ?: emptyMap()) {
        if (ALL_EXAMPLES.containsKey(p)) {
            props[p] = v.mapValue { it.copy(example = ALL_EXAMPLES[p]) }
        } else {
            props[p] = v
        }
    }

    return fullSchema.copy(
        title = title,
        properties = props,
    )
}
