package com.kai.crawler.utils

import org.jsoup.nodes.Element

/**
 *
 * @ProjectName:    bookPage-crawler
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/29 9:31
 */
class CommonUtils {
    companion object{
        /**
         * 获取同胞中同名元素的数量
         *
         * @param e
         * @return
         */
        fun sameTagElementNumber(e: Element): Int {
            val els = e.parent().getElementsByTag(e.tagName())
            return els.size
        }


        /**
         * 获取同名元素在同胞中的index
         *
         * @param e
         * @return
         */
        fun getElementIndexInSameTags(e: Element): Int {
            val chs = e.parent().children()
            var index = 1
            for (i in chs.indices) {
                val cur = chs[i]
                if (e.tagName() == cur.tagName()) {
                    index += if (e == cur) {
                        break
                    } else {
                        1
                    }
                }
            }
            return index
        }

    }
}