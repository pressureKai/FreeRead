package com.kai.crawler.entity.book

import com.kai.crawler.entity.chapter.Chapter


/**
 *#  书籍详情继承自SearchBook(搜索书籍实体类) - Crawler
 *@author pressureKai
 *@date  2021/4/13
 */
class BookDetail : SearchBook() {
    var chapters: List<Chapter>? = null
}