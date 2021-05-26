package com.kai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kai.bookpage.model.database.BookDatabase
import com.kai.database.CustomDatabase

@Entity(tableName = "UserLike")
class UserLike {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id",typeAffinity = ColumnInfo.INTEGER)
    var id: Int = 0
    var userId : Int = 0
    var bookUrl :String = ""


    fun save(){
        val bookRecommendByBookUrl =
            CustomDatabase.get().likeDao().getUserLikeById(userId,bookUrl)
        if (bookRecommendByBookUrl == null ) {
            CustomDatabase.get().likeDao().insertUserLike(this)
        }
    }
}