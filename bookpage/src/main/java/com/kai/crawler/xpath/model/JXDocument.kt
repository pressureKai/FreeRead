package com.kai.crawler.xpath.model

import com.kai.common.utils.LogUtils
import com.kai.crawler.xpath.core.XpathEvaluator
import com.kai.crawler.xpath.exception.NoSuchAxisException
import com.kai.crawler.xpath.exception.NoSuchFunctionException
import com.kai.crawler.xpath.exception.XpathSyntaxErrorException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.*

class JXDocument {
    private var elements: Elements
    private var xpathEva:XpathEvaluator ?= null

    constructor(doc: Document) {
        elements = doc.children()
        xpathEva = XpathEvaluator()
    }

    constructor(html: String?) {
        elements = Jsoup.parse(html).children()
        xpathEva = XpathEvaluator()
    }

    constructor(els: Elements) {
        elements = els
        xpathEva = XpathEvaluator()
    }

    @Throws(XpathSyntaxErrorException::class)
    fun sel(xpath: String?): List<Any?> {
        val res: MutableList<Any?> = LinkedList()
        try {
            val jns = xpathEva!!.xpathParser(xpath!!, elements)
            for (j in jns) {
                if (j!!.isText) {
                    res.add(j.textVal)
                } else {
                    res.add(j.element)
                }
            }

        } catch (e: Exception) {
            var msg: String? = "please check the xpath syntax"
            if (e is NoSuchAxisException || e is NoSuchFunctionException) {
                msg = e.message
            }
            throw XpathSyntaxErrorException(msg)
        }
        return res
    }

    @Throws(XpathSyntaxErrorException::class)
    fun selN(xpath: String?): List<JXNode?> {

        return try {
            xpathEva!!.xpathParser(xpath!!, elements)
        } catch (e: Exception) {
            LogUtils.e("Crawler","selN error is $e")
            var msg: String? = "please check the xpath syntax"
            if (e is NoSuchAxisException || e is NoSuchFunctionException) {
                msg = e.message
            }
            throw XpathSyntaxErrorException(msg)
        }
    }

    @Throws(XpathSyntaxErrorException::class)
    fun selOne(xpath: String?): Any? {
        val jxNode = selNOne(xpath)
        return if (jxNode != null) {
            if (jxNode.isText) {
                jxNode.textVal
            } else {
                jxNode.element
            }
        } else null
    }

    @Throws(XpathSyntaxErrorException::class)
    fun selNOne(xpath: String?): JXNode? {
        val jxNodeList = selN(xpath)
        return if (jxNodeList != null && jxNodeList.size > 0) {
            jxNodeList[0]
        } else null
    }
}