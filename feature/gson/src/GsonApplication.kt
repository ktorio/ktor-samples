package io.ktor.samples.gson

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.receiveOrNull
import io.ktor.response.*
import io.ktor.routing.*
import java.text.*
import java.time.*

data class Model(val name: String, val items: MutableList<Item>, val date: LocalDate = LocalDate.of(2018, 4, 13))
data class Item(val key: String, val value: String)

val model = Model("root", mutableListOf(Item("A", "Apache"), Item("B", "Bing")))

fun Application.main() {
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
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

