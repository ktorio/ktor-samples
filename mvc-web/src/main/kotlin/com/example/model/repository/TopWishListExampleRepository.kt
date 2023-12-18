package com.example.model.repository

import com.example.model.entity.Wishlist

class TopWishListExampleRepository {
    fun topWishes(): List<Wishlist> = listOf(
        Wishlist(0, "Cat"),
        Wishlist(1, "Car"),
        Wishlist(2, "Home"),
    )
}
