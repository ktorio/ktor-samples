package com.example.plugins

import com.example.controller.WishController
import com.example.exceptions.DbElementInsertException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if (cause is DbElementInsertException) {
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            } else {
                call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
            }
        }
    }
    routing {
        route("wish") {
            post("make") {
                val formWish = call.receiveParameters()
                val wishName = formWish.getOrFail("wish")
                WishController().addToWishList(wishName)
                call.respondRedirect("/wish/list")
            }
            get("list") {
                val wishList = WishController().getWishList()
                call.respond(FreeMarkerContent("wishlist.ftl", mapOf("wishList" to wishList)))
            }
            post("cancel") {
                val formId = call.receiveParameters()
                val wishId = formId.getOrFail("id").toInt()
                WishController().deleteFromWishList(wishId)
                call.respondRedirect("/wish/list")
            }
            get("topwishes") {
                val topWishList = WishController().getTopWishesExample()
                call.respond(FreeMarkerContent("topwishes.ftl", mapOf("topWishList" to topWishList)))
            }
        }

    }
}
