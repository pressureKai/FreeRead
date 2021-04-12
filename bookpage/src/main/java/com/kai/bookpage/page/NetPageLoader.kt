package com.kai.bookpage.page

import com.kai.bookpage.model.CoolBookBean
import com.kai.bookpage.model.TextChapter
import java.io.BufferedReader

/**
 *
 * @ProjectName:    NetPageLoader
 * @Description:    页面加载工具负责页面视图的绘制
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/15 10:52
 */
class NetPageLoader(pageView: PageView,coolBookBean: CoolBookBean): PageLoader(pageView,coolBookBean)  {
    override fun hasChapterData(chapter: TextChapter): Boolean {
        return false
    }

    override fun getChapterReader(chapter: TextChapter): BufferedReader {
        return BufferedReader(null)
    }

    override fun refreshChapterList() {

    }
}