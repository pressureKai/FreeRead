package com.kai.bookpage.model


/**
 *#  书籍具体内容实体类 - 每个章节的主体内容不做数据库的存储操作(只作为pageView控件中的数据转换实体)- bookPage
 *@author pressureKai
 *@date  2021/4/14
 */
class TextPage {
    var position = 0
    var title = ""
    //当前 lines 中为 title 的行数
    var titleLines = 0
    var lines: List<String> = ArrayList()
    var picture: String = ""
}





