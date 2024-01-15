package com.example.model.repository

import com.example.model.dao.wishlist.WishlistDAOFacade
import com.example.model.dao.wishlist.WishlistDAOFacadeImpl
import com.example.model.entity.Wishlist

class WishListRepository {
    private val dao: WishlistDAOFacade = WishlistDAOFacadeImpl()

    suspend fun allWishes(): List<Wishlist> {
        val listOfWishes = dao.allWishes()
        if (listOfWishes.isEmpty()) {
            return listOf(Wishlist(-1, "Make a wish"))
        }
        return dao.allWishes()
    }

    suspend fun addWish(wish: String): Wishlist {
        return dao.addWish(wish)
    }

    suspend fun deleteWish(id: Int): Boolean {
        return dao.deleteWish(id)
    }
}

