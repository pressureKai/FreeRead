package com.kai.bookpage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *# 书籍实体类
 *@author pressureKai
 *@date  2021/4/13
 */
@Entity(tableName = "coolBook")
class CoolBookBean {
    companion object{
        //未缓存
        const val STATUS_UN_CACHE = 0
        //缓存中
        const val STATUS_CACHING = 1
        //已缓存
        const val STATUS_CACHED = 2
    }
    @PrimaryKey
    @ColumnInfo(name = "bookId")
    var bookId = ""

    //标题
    @ColumnInfo(name = "title",typeAffinity = ColumnInfo.TEXT)
    var title = ""

    //作者
    @ColumnInfo(name = "author",typeAffinity = ColumnInfo.TEXT)
    var author = ""

    //短简介
    @ColumnInfo(name = "shortIntro",typeAffinity = ColumnInfo.TEXT)
    var shortIntro = ""

    //书籍封面
    @ColumnInfo(name = "cover",typeAffinity = ColumnInfo.TEXT)
    var cover = ""

    //更新日期
    @ColumnInfo(name = "updated",typeAffinity = ColumnInfo.TEXT)
    var updated = ""

    //最新阅读日期
    @ColumnInfo(name = "lastRead",typeAffinity = ColumnInfo.TEXT)
    var lastRead = ""

    //最新阅读章节
    @ColumnInfo(name = "lastChapter",typeAffinity = ColumnInfo.TEXT)
    var lastChapter = ""

    //章节总数
    @ColumnInfo(name = "chapterCount",typeAffinity = ColumnInfo.INTEGER)
    var chapterCount = 0

    //是否更新
    @ColumnInfo(name = "isUpdate")
    var isUpdate = false

    //是否为本地书籍
    @ColumnInfo(name = "isLocal")
    var isLocal = false
}