package com.kai.bookpage.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 *# room多表查询实体类 表示 书本与章节的一对多关系
 *@author  pressureKai
 *@date  2021/4/15
 */
class BookChapterListBean {
    @Embedded
    var coolBookBean: CoolBookBean ?= null

    @Relation(parentColumn = "bookId",entityColumn = "bookId")
    var bookChapterList: List<BookChapterBean> = ArrayList()
}