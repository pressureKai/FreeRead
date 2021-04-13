package com.kai.crawler.entity.book

import com.kai.crawler.entity.source.Source


/**
 *# 搜索书籍实体类 - Crawler
 *@author pressureKai
 *@date  2021/4/13
 */
open class SearchBook{
    var cover: String = ""
    var title: String = ""
    var author: String = ""
    var descriptor : String = ""
    var sources: ArrayList<SL> = ArrayList()

    /**
     *#  书源实体类 - Crawler (链接对应一个实体)
     *@author pressureKai
     *@date  2021/4/13
     */
    class SL(link: String, source: Source) {
        var link: String = link
        var source: Source = source
    }
}