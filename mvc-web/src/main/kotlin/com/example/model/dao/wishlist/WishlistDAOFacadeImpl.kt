package com.example.model.dao.wishlist

import com.example.exceptions.DbElementInsertException
import com.example.model.DatabaseSingleton.dbQuery
import com.example.model.entity.Wishlist
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class WishlistDAOFacadeImpl : WishlistDAOFacade {

    private fun resultRowToWish(row: ResultRow) = Wishlist(
        id = row[Wishlists.id],
        wish = row[Wishlists.wish]
    )

    override suspend fun allWishes(): List<Wishlist> = dbQuery {
        Wishlists.selectAll().map(::resultRowToWish)
    }

    override suspend fun addWish(wish: String): Wishlist = dbQuery {
        val insertStatement = Wishlists.insert {
            it[Wishlists.wish] = wish
        }
        val addedWish = insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToWish)
        if (addedWish == null) {
            throw DbElementInsertException("You've exceeded your wish limit")
        } else {
            return@dbQuery addedWish
        }
    }

    override suspend fun deleteWish(id: Int): Boolean = dbQuery {
        Wishlists.deleteWhere { Wishlists.id eq id } > 0
    }
}