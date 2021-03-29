package com.kai.crawler.xpath.core

import com.kai.crawler.xpath.model.Node
import java.util.*

class XContext() {
    var xpathTr: LinkedList<Node> ?= null

    init {
        xpathTr = LinkedList()
    }
}