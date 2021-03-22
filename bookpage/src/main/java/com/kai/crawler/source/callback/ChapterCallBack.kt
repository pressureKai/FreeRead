package com.kai.crawler.source.callback

import com.kai.bookpage.model.TextChapter

/**
 *
 * @ProjectName:    bookpage
 * @Description:    爬虫章节回调
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:00
 */
interface ChapterCallBack {
    fun onResponse(chapters: List<TextChapter>)
    fun onError(msg: String)
}