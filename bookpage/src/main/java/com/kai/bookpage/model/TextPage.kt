package com.kai.bookpage.model



/**
 *#  页面实体类 一个TextPage 代表一页  多页数据 来自同一章节
 *   position 代表当前页码
 *   title 所属章节标题
 *   lines 字符串行数
 *@author pressureKai
 *@date  2021/4/14
 */
class TextPage {
    var position = 0
    var title = ""
    var lines: ArrayList<String> = ArrayList()
}





