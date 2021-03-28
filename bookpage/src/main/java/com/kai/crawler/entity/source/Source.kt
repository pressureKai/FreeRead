package com.kai.crawler.entity.source

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/22 11:14
 */
class Source(sourceID: Int, sourceName: String, searchUrl: String) {
    var id = sourceID
    var name = sourceName
    var searchUrl = searchUrl
    var minKeyWord = ""
}