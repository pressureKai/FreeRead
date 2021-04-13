package com.kai.bookpage.page

import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.TextChapter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 *
 * @ProjectName:    LocalPageLoader
 * @Description:    页面加载工具负责页面视图的绘制
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/15 10:42
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