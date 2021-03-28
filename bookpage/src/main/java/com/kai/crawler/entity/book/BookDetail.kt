package com.kai.crawler.entity.book

import com.kai.crawler.entity.chapter.Chapter


/**
 * 书籍详情
 */
class BookDetail : SearchBook() {
    var chapters: List<Chapter>? = null
}