package com.kai.bookpage.model


/**
 *#  书籍章节列表实体类,作数据库存储操作
 *@author pressureKai
 *@date  2021/4/14
 */
class BookChapterBean {
    var id = ""
    var link = ""
    var title = ""
    var taskName = ""
    //是否可读
    var unReadable = false
    //书籍所属id
    var bookId = ""

    //此章节在本地书籍文件中的起始位置
    var start = 0L

    //此章节在本地书籍文件中的结束位置
    var end = 0L
}