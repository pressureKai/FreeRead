package com.kai.crawler.entity.book

import com.kai.crawler.entity.source.Source

/**
 *
 * @ProjectName:    bookpage
 * @Description:    搜索书籍数据格式
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:10
 */
open class SearchBook{
    var cover: String = ""
    var title: String = ""
    var author: String = ""
    var descriptor : String = ""
    var sources: ArrayList<SL> = ArrayList()

    class SL{
        var link: String = ""
        var source: Source = Source()
    }
}