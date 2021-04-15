package com.kai.bookpage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey


/**
 *#  书籍章节列表实体类,作数据库存储操作
 *@author pressureKai
 *@date  2021/4/14
 */
@Entity(tableName = "bookChapter",foreignKeys =
[ForeignKey(entity = CoolBookBean::class,
        parentColumns = arrayOf("bookId"),
        childColumns = arrayOf("bookId"),
        onDelete =  CASCADE)])
class BookChapterBean {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id",typeAffinity = ColumnInfo.INTEGER)
    var id = 0
    var link = ""
    var title = ""
    var taskName = ""
    //是否可读
    var unReadable = false
    //书籍所属id
    var bookId = 0
    //此章节在本地书籍文件中的起始位置
    var start = 0L
    //此章节在本地书籍文件中的结束位置
    var end = 0L
}