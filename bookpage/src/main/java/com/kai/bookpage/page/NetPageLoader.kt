package com.kai.bookpage.page

import com.kai.bookpage.model.BookChapterBean
import com.kai.bookpage.model.CoolBookBean
import com.kai.common.utils.LogUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 *# 网络数据加载器 - 从网络获取数据进行加载
 *@author pressureKai
 *@date  2021/4/14
 */
class NetPageLoader(pageView: PageView,coolBookBean: CoolBookBean): PageLoader(pageView,coolBookBean)  {
    override fun hasChapterData(chapter: BookChapterBean): Boolean {
        return false
    }

    override fun getChapterReader(chapter: BookChapterBean): BufferedReader {
        //从文件中获取数据
        LogUtils.e("PageView","get Chapter reader")
        val content: ByteArray = "456465465".toByteArray()
        val bais = ByteArrayInputStream(content)
        val br = BufferedReader(InputStreamReader(bais, "test"))
        return br
    }

    override fun refreshChapterList() {

    }


    override fun saveRecord() {
        super.saveRecord()
    }

}