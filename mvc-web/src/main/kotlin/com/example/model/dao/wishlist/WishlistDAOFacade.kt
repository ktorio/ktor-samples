package com.example.model.dao.wishlist

import com.example.model.entity.Wishlist

interface WishlistDAOFacade {
    suspend fun allWishes(): List<Wishlist>
    suspend fun addWish(wish: String): Wishlist
    suspend fun deleteWish(id: Int): Boolean
}