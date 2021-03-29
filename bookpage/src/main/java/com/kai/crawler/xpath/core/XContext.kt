package com.kai.crawler.xpath.core

import com.kai.crawler.xpath.model.Node
import java.util.*

class XContext {
    lateinit var xpathTr: LinkedList<Node>

    init {
        if (xpathTr == null) {
            xpathTr = LinkedList()
        }
    }
}