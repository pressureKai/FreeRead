package com.kai.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kai.entity.SearchHistory

@Dao
interface SearchHistoryDao {
    @Delete
    fun deleteHistory(history: SearchHistory)

    @Insert
    fun insertHistory(history: SearchHistory)

    @Query("SELECT *  FROM SearchHistory WHERE isRecommend = :isRecommend")
    fun getHistoryList(isRecommend : Boolean): List<SearchHistory>

    @Query("SELECT *  FROM SearchHistory WHERE searchName = :name and isRecommend = :isRecommend")
    fun getHistoryByName(name : String,isRecommend: Boolean): List<SearchHistory>


}