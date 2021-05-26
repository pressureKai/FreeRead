package com.kai.dao

import androidx.room.*
import com.kai.entity.User
import com.kai.entity.UserLike

@Dao
interface LikeDao {
    @Insert
    fun insertUserLike(userLike: UserLike)

    @Delete
    fun deleteUserLike(userLike: UserLike)

    @Update
    fun updateUserLike(userLike: UserLike)

    @Query("SELECT * FROM UserLike WHERE userId = :userId")
    fun getUserLikeList(userId: Int): List<UserLike>

    @Query("SELECT * FROM UserLike WHERE userId = :userId AND bookUrl = :bookUrl")
    fun getUserLikeById(userId: Int,bookUrl:String): List<UserLike>
}