package com.kai.crawler.entity.source

/**
 * 默认配置
 * 可能有部分源，比较复杂，需要多个xpath，那就继承重写
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

    class Search {
        var charset: String? = null
        var xpath: String? = null
        var coverXpath: String? = null
        var titleXpath: String? = null
        var linkXpath: String? = null
        var authorXpath: String? = null
        var descXpath: String? = null
    }

    class Catalog {
        var xpath: String? = null
        var titleXpath: String? = null
        var linkXpath: String? = null
    }

    class Content {
        var xpath: String? = null
    }
}