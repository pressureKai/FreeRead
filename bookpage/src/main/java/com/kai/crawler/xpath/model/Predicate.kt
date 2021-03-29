package com.kai.crawler.xpath.model

import com.kai.crawler.utils.OpEm

/**
 * xpath语法节点的谓语部分，即要满足的限定条件
 */
class Predicate {
    var opEm: OpEm? = null
    var left: String? = null
    var right: String? = null
    var value: String? = null
}