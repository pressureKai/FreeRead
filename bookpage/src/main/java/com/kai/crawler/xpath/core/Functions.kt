package com.kai.crawler.xpath.core

import com.kai.crawler.utils.CommonUtils.Companion.getElementIndexInSameTags
import com.kai.crawler.utils.CommonUtils.Companion.sameTagElementNumber
import com.kai.crawler.xpath.model.JXNode
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*
import java.util.regex.Pattern

/*
  Copyright 2014 Wang Haomiao<et.tw@163.com>

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
/**
 * xpath解析器的支持的全部函数集合，如需扩展按形式添加即可
 */
class Functions {
    /**
     * 只获取节点自身的子文本
     *
     * @param context
     * @return
     */
    fun text(context: Elements?): List<JXNode> {
        val res: MutableList<JXNode> = LinkedList()
        if (context != null && context.size > 0) {
            for (e in context) {
                if (e.nodeName() == "script") {
                    res.add(JXNode.t(e.data()))
                } else {
                    res.add(JXNode.t(e.ownText()))
                }
            }
        }
        return res
    }

    /**
     * 递归获取节点内全部的纯文本
     *
     * @param context
     * @return
     */
    fun allText(context: Elements?): List<JXNode> {
        val res: MutableList<JXNode> = LinkedList()
        if (context != null && context.size > 0) {
            for (e in context) {
                res.add(JXNode.t(e.text()))
            }
        }
        return res
    }

    /**
     * 获取全部节点的内部的html
     *
     * @param context
     * @return
     */
    fun html(context: Elements?): List<JXNode> {
        val res: MutableList<JXNode> = LinkedList()
        if (context != null && context.size > 0) {
            for (e in context) {
                res.add(JXNode.t(e.html()))
            }
        }
        return res
    }

    /**
     * 获取全部节点的 包含节点本身在内的全部html
     *
     * @param context
     * @return
     */
    fun outerHtml(context: Elements?): List<JXNode> {
        val res: MutableList<JXNode> = LinkedList()
        if (context != null && context.size > 0) {
            for (e in context) {
                res.add(JXNode.t(e.outerHtml()))
            }
        }
        return res
    }

    /**
     * 获取全部节点
     *
     * @param context
     * @return
     */
    fun node(context: Elements?): List<JXNode> {
        return html(context)
    }

    /**
     * 抽取节点自有文本中全部数字
     *
     * @param context
     * @return
     */
    fun num(context: Elements?): List<JXNode> {
        val res: MutableList<JXNode> = LinkedList()
        if (context != null) {
            val pattern = Pattern.compile("\\d+")
            for (e in context) {
                val matcher = pattern.matcher(e.ownText())
                if (matcher.find()) {
                    res.add(JXNode.t(matcher.group()))
                }
            }
        }
        return res
    }
    /**
     * =====================
     * 下面是用于过滤器的函数
     */
    /**
     * 获取元素自己的子文本
     *
     * @param e
     * @return
     */
    fun text(e: Element): String {
        return e.ownText()
    }

    /**
     * 获取元素下面的全部文本
     *
     * @param e
     * @return
     */
    fun allText(e: Element): String {
        return e.text()
    }

    /**
     * 判断一个元素是不是最后一个同名同胞中的
     *
     * @param e
     * @return
     */
    fun last(e: Element?): Boolean {
        return getElementIndexInSameTags(e!!) == sameTagElementNumber(
            e
        )
    }

    /**
     * 判断一个元素是不是同名同胞中的第一个
     *
     * @param e
     * @return
     */
    fun first(e: Element?): Boolean {
        return getElementIndexInSameTags(e!!) == 1
    }

    /**
     * 返回一个元素在同名兄弟节点中的位置
     *
     * @param e
     * @return
     */
    fun position(e: Element?): Int {
        return getElementIndexInSameTags(e!!)
    }

    /**
     * 判断是否包含
     *
     * @param left
     * @param right
     * @return
     */
    fun contains(left: String, right: String?): Boolean {
        return left.contains(right!!)
    }
}