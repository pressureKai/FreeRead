package com.kai.dao

import androidx.room.*
import com.kai.entity.User
import com.kai.entity.UserShelf

@Dao
interface ShelfDao {
    @Insert
    fun insertUserShelf(userShelf: UserShelf)

    @Delete
    fun deleteUserShelf(userShelf: UserShelf)

    @Update
    fun updateUserShelf(user: UserShelf)

    @Query("SELECT * FROM UserShelf WHERE userId = :userId")
    fun getUserShelfList(userId: Int): List<UserShelf>

    @Query("SELECT * FROM UserShelf WHERE userId = :userId AND bookUrl =:bookUrl")
    fun getUserShelfById(userId: Int,bookUrl:String): List<UserShelf>

}