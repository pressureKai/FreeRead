package com.kai.bookpage.page

import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.TextChapter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 *# 本地数据加载器,依靠解析本地数据来提供书籍内容（每章内容与书籍章节列表等）
 *@author pressureKai
 *@date  2021/4/14
 */
class LocalPageLoader(pageView: PageView, coolBookBean: CoolBookBean): PageLoader(pageView, coolBookBean) {
    init {
        mStatus = STATUS_PARING
    }
    override fun hasChapterData(chapter: TextChapter): Boolean {
        return true
    }

    override fun getChapterReader(chapter: TextChapter): BufferedReader {

        //从文件中获取数据
        val content: ByteArray = "456465465".toByteArray()
        val bais = ByteArrayInputStream(content)
        val br = BufferedReader(InputStreamReader(bais, "test"))
        return br
    }

    override fun refreshChapterList() {

    }


}