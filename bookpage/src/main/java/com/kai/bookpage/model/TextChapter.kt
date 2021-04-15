package com.kai.bookpage.model


/**
 *# 章节实体类 - 不做存储操作,只在PageView中做章节数据转换的中间实体 - bookPage
 *@author pressureKai
 *@date  2021/4/14
 */
class TextChapter {
    //在CoolBookBean 中的书籍id
    var bookId = 0
    //书籍所对应的网络链接
    var link  = ""
    //标题
    var title = ""
    //此章节在文件中的开始位置(适用于本地书籍)
    var start = 0L
    //此章节在文件中的结束位置(适用于本地书籍)
    var end = 0L
}