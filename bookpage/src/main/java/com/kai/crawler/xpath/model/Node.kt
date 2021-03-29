package com.kai.crawler.xpath.model

/**
 * xpath语法链的一个基本节点
 */
class Node {
    var scopeEm: ScopeEm? = null
    var axis: String? = null
    var tagName: String? = null
    var predicate: Predicate? = null
}