package com.kai.crawler.xpath.model

import com.kai.crawler.xpath.exception.XpathSyntaxErrorException
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * XPath提取后的 节点
 */
class JXNode {
    var element: Element? = null
        private set
    var isText = false
        private set
    var textVal: String? = null
        private set

    fun setElement(element: Element?): JXNode {
        this.element = element
        return this
    }

    fun setText(text: Boolean): JXNode {
        isText = text
        return this
    }

    fun setTextVal(textVal: String?): JXNode {
        this.textVal = textVal
        return this
    }

    @Throws(XpathSyntaxErrorException::class)
    fun sel(xpath: String?): List<JXNode>? {
        if (element == null) {
            return null
        }
        val doc = JXDocument(Elements(element))
        return doc.selN(xpath)
    }

    override fun toString(): String {
        return if (isText) {
            textVal!!
        } else {
            element.toString()
        }
    }

    companion object {
        @JvmStatic
        fun e(element: Element?): JXNode {
            val n = JXNode()
            n.setElement(element).setText(false)
            return n
        }

        @JvmStatic
        fun t(txt: String?): JXNode {
            val n = JXNode()
            n.setTextVal(txt).setText(true)
            return n
        }
    }
}