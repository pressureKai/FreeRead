package com.kai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kai.database.CustomDatabase

@Entity(tableName = "UserShelf")
class UserShelf {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id",typeAffinity = ColumnInfo.INTEGER)
    var id: Int = 0
    var userId : Int = 0
    var bookUrl :String = ""

    fun save(){
        val bookRecommendByBookUrl =
            CustomDatabase.get().shelfDao().getUserShelfById(userId,bookUrl)
        if (bookRecommendByBookUrl == null ) {
            CustomDatabase.get().shelfDao().insertUserShelf(this)
        }
    }
}