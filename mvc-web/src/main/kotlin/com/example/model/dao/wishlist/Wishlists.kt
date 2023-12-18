package com.example.model.dao.wishlist

import org.jetbrains.exposed.sql.Table

object Wishlists : Table() {
    val id = integer("id").autoIncrement()
    val wish = varchar("body", 1024)

    override val primaryKey = PrimaryKey(id)
}