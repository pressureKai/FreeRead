package com.kai.bookpage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *# 存储阅读记录 - bookPage
 *@author pressureKai
 *@date  2021/4/14
 */
@Entity(tableName = "bookRecord")
class BookRecordBean {
     @PrimaryKey(autoGenerate = true)
     @ColumnInfo(name = "id",typeAffinity = ColumnInfo.INTEGER)
     var id = 0
     var bookId = 0
     var chapter = 0
     var pagePos = 0
}
