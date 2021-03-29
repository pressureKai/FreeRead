package com.kai.crawler.xpath.core

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * 通过轴选出对应作用域的全部节点
 * 去掉不实用的轴，不支持namespace，attribute（可用 /@*代替），preceding(preceding-sibling支持)，following(following-sibling支持)
 * 添加 preceding-sibling-one，following-sibling-one,即只选前一个或后一个兄弟节点，添加 sibling 选取全部兄弟节点
 */
class AxisSelector {
    /**
     * 自身
     *
     * @param e
     * @return
     */
    fun self(e: Element?): Elements {
        return Elements(e)
    }

    /**
     * 父节点
     *
     * @param e
     * @return
     */
    fun parent(e: Element): Elements {
        return Elements(e.parent())
    }

    /**
     * 直接子节点
     *
     * @param e
     * @return
     */
    fun child(e: Element): Elements {
        return e.children()
    }

    /**
     * 全部祖先节点 父亲，爷爷 ， 爷爷的父亲...
     *
     * @param e
     * @return
     */
    fun ancestor(e: Element): Elements {
        return e.parents()
    }

    /**
     * 全部祖先节点和自身节点
     *
     * @param e
     * @return
     */
    fun ancestorOrSelf(e: Element): Elements {
        val rs = e.parents()
        rs.add(e)
        return rs
    }

    /**
     * 全部子代节点 儿子，孙子，孙子的儿子...
     *
     * @param e
     * @return
     */
    fun descendant(e: Element): Elements {
        return e.allElements
    }

    /**
     * 全部子代节点和自身
     *
     * @param e
     * @return
     */
    fun descendantOrSelf(e: Element): Elements {
        val rs = e.allElements
        rs.add(e)
        return rs
    }

    /**
     * 节点前面的全部同胞节点，preceding-sibling
     *
     * @param e
     * @return
     */
    fun precedingSibling(e: Element): Elements {
        val rs = Elements()
        var tmp = e.previousElementSibling()
        while (tmp != null) {
            rs.add(tmp)
            tmp = tmp.previousElementSibling()
        }
        return rs
    }

    /**
     * 返回前一个同胞节点（扩展），语法 preceding-sibling-one
     *
     * @param e
     * @return
     */
    fun precedingSiblingOne(e: Element): Elements {
        val rs = Elements()
        if (e.previousElementSibling() != null) {
            rs.add(e.previousElementSibling())
        }
        return rs
    }

    /**
     * 节点后面的全部同胞节点following-sibling
     *
     * @param e
     * @return
     */
    fun followingSibling(e: Element): Elements {
        val rs = Elements()
        var tmp = e.nextElementSibling()
        while (tmp != null) {
            rs.add(tmp)
            tmp = tmp.nextElementSibling()
        }
        return rs
    }

    /**
     * 返回下一个同胞节点(扩展) 语法 following-sibling-one
     *
     * @param e
     * @return
     */
    fun followingSiblingOne(e: Element): Elements {
        val rs = Elements()
        if (e.nextElementSibling() != null) {
            rs.add(e.nextElementSibling())
        }
        return rs
    }

    /**
     * 全部同胞（扩展）
     *
     * @param e
     * @return
     */
    fun sibling(e: Element): Elements {
        return e.siblingElements()
    }
}