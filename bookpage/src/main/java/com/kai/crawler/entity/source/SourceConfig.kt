package com.kai.crawler.entity.source

/**

 */

/**
 *# 默认配置 可能有部分源，比较复杂，需要多个xpath，那就继承重写 - Crawler
 *@author pressureKai
 *@date  2021/4/13
 */
class SourceConfig(var id: Int) {

    /**
     * 搜索
     */
    var search: Search? = null

    /**
     * 小说目录内容
     */
    var catalog: Catalog? = null

    /**
     * 小说内容
     */
    var content: Content? = null


    var home: Home?= null

    /**
     *# 搜索XPath配置 - Crawler
     *@author pressureKai
     *@date  2021/4/13
     */
    class Search {
        var charset: String? = null
        var xpath: String? = null
        var coverXpath: String? = null
        var titleXpath: String? = null
        var linkXpath: String? = null
        var authorXpath: String? = null
        var descXpath: String? = null
    }

    /**
     *#  目录 XPath配置 - Crawler
     *@author pressureKai
     *@date  2021/4/13
     */
    class Catalog {
        var xpath: String? = null
        var titleXpath: String? = null
        var linkXpath: String? = null
    }

    /**
     * # 主题内容 XPath配置 - Crawler
     *@author pressureKai
     *@date  2021/4/13
     */
    class Content {
        var xpath: String? = null
    }

    class Home {
        var recommendPath: String? = null
        var recommendNamePath: String ?= null
    }
}