package com.example.controller

import com.example.model.entity.Wishlist
import com.example.model.repository.TopWishListExampleRepository
import com.example.model.repository.WishListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WishController {
    private val topWishListExampleRepository = TopWishListExampleRepository()
    private val wishListRepository = WishListRepository()

    suspend fun addToWishList(wish: String): Wishlist = withContext(Dispatchers.IO) { wishListRepository.addWish(wish) }

    suspend fun getWishList(): List<Wishlist> = run { wishListRepository.allWishes() }

    suspend fun deleteFromWishList(recordId: Int) = run { wishListRepository.deleteWish(recordId) }

    fun getTopWishesExample(): List<Wishlist> = run { topWishListExampleRepository.topWishes() }
}