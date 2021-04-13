package com.kai.crawler.entity.source


/**
 *# 书源实体 - Crawler
 *@author pressureKai
 *@date  2021/4/13
 */
class Source(sourceID: Int, sourceName: String, searchUrl: String) {
    var id = sourceID
    var name = sourceName
    var searchUrl = searchUrl
    var minKeyWord = ""
}