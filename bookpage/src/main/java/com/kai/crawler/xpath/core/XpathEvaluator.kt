package com.kai.crawler.xpath.core

import com.kai.crawler.utils.CommonUtils.Companion.getElementIndexInSameTags
import com.kai.crawler.utils.CommonUtils.Companion.getJMethodNameFromStr
import com.kai.crawler.xpath.core.SingletonProducer.Companion.instance
import com.kai.crawler.xpath.exception.NoSuchAxisException
import com.kai.crawler.xpath.exception.NoSuchFunctionException
import com.kai.crawler.xpath.model.JXNode
import com.kai.crawler.xpath.model.JXNode.Companion.e
import com.kai.crawler.xpath.model.JXNode.Companion.t
import com.kai.crawler.xpath.model.Node
import com.kai.crawler.xpath.model.ScopeEm
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.lang.reflect.Method
import java.util.*

class XpathEvaluator {
    private val emFuncs: MutableMap<String, Method>
    private val axisFuncs: MutableMap<String, Method>

    /**
     * xpath解析器的总入口，同时预处理，如‘|’
     *
     * @param xpath
     * @param root
     * @return
     */
    @Throws(NoSuchAxisException::class, NoSuchFunctionException::class)
    fun xpathParser(xpath: String, root: Elements): List<JXNode?> {
        return if (xpath.contains("|")) {
            val rs: MutableList<JXNode?> = LinkedList()
            val chiXpaths = xpath.split("\\|".toRegex()).toTypedArray()
            for (chiXp in chiXpaths) {
                if (chiXp.length > 0) {
                    rs.addAll(evaluate(chiXp.trim { it <= ' ' }, root))
                }
            }
            rs
        } else {
            evaluate(xpath, root)
        }
    }

    /**
     * 获取xpath解析语法树
     *
     * @param xpath
     * @return
     */
    fun getXpathNodeTree(xpath: String?): List<Node> {
        val st = NodeTreeBuilderStateMachine()
        while (st.state !== NodeTreeBuilderStateMachine.BuilderState.END) {
            st.state.parser(st, xpath!!.toCharArray())
        }
        return st.context.xpathTr!!
    }

    /**
     * 根据xpath求出结果
     *
     * @param xpath
     * @param root
     * @return
     */
    @Throws(NoSuchAxisException::class, NoSuchFunctionException::class)
    fun evaluate(xpath: String?, root: Elements): List<JXNode?> {
        var res: MutableList<JXNode?> = LinkedList()
        var context = root
        val xpathNodes = getXpathNodeTree(xpath)
        for (i in xpathNodes.indices) {
            val n = xpathNodes[i]
            val contextTmp = LinkedList<Element>()
            if (n.scopeEm === ScopeEm.RECURSIVE || n.scopeEm === ScopeEm.CURREC) {
                if (n.tagName!!.startsWith("@")) {
                    for (e in context) {
                        //处理上下文自身节点
                        val key = n.tagName!!.substring(1)
                        if (key == "*") {
                            res.add(t(e.attributes().toString()))
                        } else {
                            val value = e.attr(key)
                            if (StringUtils.isNotBlank(value)) {
                                res.add(t(value))
                            }
                        }
                        //处理上下文子代节点
                        for (dep in e.allElements) {
                            if (key == "*") {
                                res.add(t(dep.attributes().toString()))
                            } else {
                                val value = dep.attr(key)
                                if (StringUtils.isNotBlank(value)) {
                                    res.add(t(value))
                                }
                            }
                        }
                    }
                } else if (n.tagName!!.endsWith("()")) {
                    //递归执行方法默认只支持text()
                    res.add(t(context.text()))
                } else {
                    val searchRes = context.select(n.tagName)
                    for (e in searchRes) {
                        val filterR = filter(e, n)
                        if (filterR != null) {
                            contextTmp.add(filterR)
                        }
                    }
                    context = Elements(contextTmp)
                    if (i == xpathNodes.size - 1) {
                        for (e in contextTmp) {
                            res.add(e(e))
                        }
                    }
                }
            } else {
                if (n.tagName!!.startsWith("@")) {
                    for (e in context) {
                        val key = n.tagName!!.substring(1)
                        if (key == "*") {
                            res.add(t(e.attributes().toString()))
                        } else {
                            val value = e.attr(key)
                            if (StringUtils.isNotBlank(value)) {
                                res.add(t(value))
                            }
                        }
                    }
                } else if (n.tagName!!.endsWith("()")) {
                    res = callFunc(
                        n.tagName!!.substring(0, n.tagName!!.length - 2),
                        context
                    ) as MutableList<JXNode?>
                } else {
                    for (e in context) {
                        var filterScope = e.children()
                        if (StringUtils.isNotBlank(n.axis)) {
                            filterScope = getAxisScopeEls(n.axis, e)
                        }
                        for (chi in filterScope) {
                            val fchi = filter(chi, n)
                            if (fchi != null) {
                                contextTmp.add(fchi)
                            }
                        }
                    }
                    context = Elements(contextTmp)
                    if (i == xpathNodes.size - 1) {
                        for (e in contextTmp) {
                            res.add(e(e))
                        }
                    }
                }
            }
        }
        return res
    }

    /**
     * 元素过滤器
     *
     * @param e
     * @param node
     * @return
     */
    @Throws(NoSuchFunctionException::class, NoSuchAxisException::class)
    fun filter(e: Element, node: Node): Element? {
        if (node.tagName == "*" || node.tagName == e.nodeName()) {
            if (node.predicate != null && StringUtils.isNotBlank(node.predicate!!.value)) {
                val p = node.predicate
                if (p!!.opEm == null) {
                    if (p.value!!.matches(Regex("\\d+")) && getElIndex(e) == p.value!!.toInt()) {
                        return e
                    } else if (p.value!!.endsWith("()") && callFilterFunc(
                            p.value!!.substring(
                                0,
                                p.value!!.length - 2
                            ), e
                        ) as Boolean
                    ) {
                        return e
                    } else if (p.value!!.startsWith("@") && e.hasAttr(
                            StringUtils.substringAfter(
                                p.value, "@"
                            )
                        )
                    ) {
                        return e
                    }
                } else {

                    if (p.left!!.matches( Regex("[^/]+\\(\\)"))) {
                        val filterRes = p.opEm!!.execute(
                            callFilterFunc(
                                p.left!!.substring(
                                    0,
                                    p.left!!.length - 2
                                ), e
                            ).toString(), p.right!!
                        )
                        if (filterRes is Boolean && filterRes) {
                            return e
                        } else if (filterRes is Int && e.siblingIndex() == filterRes.toString()
                                .toInt()
                        ) {
                            return e
                        }
                    } else if (p.left!!.startsWith("@")) {
                        val lValue = e.attr(p.left!!.substring(1))
                        val filterRes = p.opEm!!.execute(lValue, p.right!!)
                        if ((filterRes as Boolean?)!!) {
                            return e
                        }
                    } else {
                        // 操作符左边不是函数、属性默认就是xpath表达式了
                        val eltmp: MutableList<Element> = LinkedList()
                        eltmp.add(e)
                        val rstmp = evaluate(p.left, Elements(eltmp))
                        if ((p.opEm!!.execute(
                                StringUtils.join(rstmp, ""),
                                p.right!!
                            ) as Boolean?)!!
                        ) {
                            return e
                        }
                    }
                }
            } else {
                return e
            }
        }
        return null
    }

    /**
     * 调用轴选择器
     *
     * @param axis
     * @param e
     * @return
     * @throws NoSuchAxisException
     */
    @Throws(NoSuchAxisException::class)
    fun getAxisScopeEls(axis: String?, e: Element): Elements {
        return try {
            val functionName = getJMethodNameFromStr(axis!!)
            val axisSelector = axisFuncs[renderFuncKey(functionName, e.javaClass)]
            axisSelector!!.invoke(instance.axisSelector, e) as Elements
        } catch (e1: Exception) {
            throw NoSuchAxisException("this axis is not supported,plase use other instead of '$axis'")
        }
    }

    /**
     * 调用xpath主干上的函数
     *
     * @param funcname
     * @param context
     * @return
     * @throws NoSuchFunctionException
     */
    @Throws(NoSuchFunctionException::class)
    fun callFunc(funcname: String?, context: Elements): Any {
        return try {
            val function = emFuncs[renderFuncKey(funcname, context.javaClass)]
            function!!.invoke(instance.functions, context)
        } catch (e: Exception) {
            throw NoSuchFunctionException("This function is not supported")
        }
    }

    /**
     * 调用谓语中函数
     *
     * @param funcname
     * @param el
     * @return
     * @throws NoSuchFunctionException
     */
    @Throws(NoSuchFunctionException::class)
    fun callFilterFunc(funcname: String?, el: Element): Any {
        return try {
            val function = emFuncs[renderFuncKey(funcname, el.javaClass)]
            function!!.invoke(instance.functions, el)
        } catch (e: Exception) {
            throw NoSuchFunctionException("This function is not supported")
        }
    }

    fun getElIndex(e: Element?): Int {
        return if (e != null) {
            getElementIndexInSameTags(e)
        } else 1
    }

    private fun renderFuncKey(funcName: String?, vararg params: Class<*>): String {
        return funcName + "|" + StringUtils.join(params, ",")
    }

    init {
        emFuncs = HashMap()
        axisFuncs = HashMap()
        for (m in Functions::class.java.declaredMethods) {
            emFuncs[renderFuncKey(m.name, *m.parameterTypes)] = m
        }
        for (m in AxisSelector::class.java.declaredMethods) {
            axisFuncs[renderFuncKey(m.name, *m.parameterTypes)] = m
        }
    }
}