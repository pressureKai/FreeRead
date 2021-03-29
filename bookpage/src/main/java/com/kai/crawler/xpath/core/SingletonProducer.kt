package com.kai.crawler.xpath.core

class SingletonProducer private constructor() {
    val axisSelector = AxisSelector()
    val functions = Functions()

    companion object {
        val instance = SingletonProducer()
    }
}