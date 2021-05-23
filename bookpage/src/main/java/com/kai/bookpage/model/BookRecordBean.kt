package com.kai.bookpage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kai.bookpage.model.database.BookDatabase
import com.kai.common.utils.LogUtils

/**
 *# 存储阅读记录 - bookPage
 *@author pressureKai
 *@date  2021/4/14
 */
@Entity(tableName = "bookRecord")
class BookRecordBean {
     @PrimaryKey(autoGenerate = true)
     @ColumnInfo(name = "id")
     var id = 0
     var bookId = ""
     var chapter = 0
     var pagePos = 0
     var userId = 0L


     fun save(){
          LogUtils.e("BookRecord","save now")
          val bookRecord = BookDatabase.get().bookDao().getBookRecord(bookId)
          if(bookRecord == null){
               LogUtils.e("BookRecord","save now ${this.bookId}")
               BookDatabase.get().bookDao().insertBookRecord(this)
          } else {
               bookRecord.chapter = chapter
               bookRecord.pagePos = pagePos
               BookDatabase.get().bookDao().updateBookRecord(bookRecord)
               LogUtils.e("BookRecord","re save now ${this.bookId}")
          }
     }
}
