package com.kai.crawler.callback

import com.kai.bookpage.model.BookChapterBean

/**
 *
 * @ProjectName:    bookpage
 * @Description:    爬虫章节回调
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:00
 */
interface ChapterCallBack {
    fun onResponse(chapters: List<BookChapterBean>)
    fun onError(msg: String)
}