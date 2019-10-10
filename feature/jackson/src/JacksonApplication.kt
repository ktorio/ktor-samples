package io.ktor.samples.jackson

import com.fasterxml.jackson.core.util.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.receiveOrNull
import io.ktor.response.*
import io.ktor.routing.*
import java.time.*

data class Model(val name: String, val items: MutableList<Item>, val date: LocalDate = LocalDate.of(2018, 4, 13))
data class Item(val key: String, val value: String)

val model = Model("root", mutableListOf(Item("A", "Apache"), Item("B", "Bing")))

fun Application.main() {
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                indentObjectsWith(DefaultIndenter("  ", "\n"))
            })
            registerModule(JavaTimeModule())  // support java.time.* types
        }
    }
    routing {
        get("/v1") {
            call.respond(model)
        }
        get("/v1/item/{key}") {
            val item = model.items.firstOrNull { it.key == call.parameters["key"] }
            if (item == null)
                call.respond(HttpStatusCode.NotFound)
            else
                call.respond(item)
        }
        post("/v1/upload") {
            val item = call.receiveOrNull<Item>()
            if(item == null)
                call.respond(HttpStatusCode.BadRequest, "You must provide a key and a value")
            else {
                model.items += item
                call.respond("${item.key} uploaded.\n")
            }
        }
    }
}

