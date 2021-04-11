package com.kai.dao

import androidx.room.*
import com.kai.entity.User

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User)

    @Delete
    fun deleteUser(user: User)

    @Update
    fun updateUser(user: User)

    @Query("SELECT * FROM USER")
    fun getUserList(): List<User>

    @Query("SELECT * FROM USER WHERE id = :id")
    fun getUserById(id: Int): User
}