package com.kai.crawler.xpath.core

import com.kai.crawler.utils.EmMap.Companion.instance
import com.kai.crawler.xpath.model.Node
import com.kai.crawler.xpath.model.Predicate

/**
 * 用于生成xpath语法树的有限状态机
 */
class NodeTreeBuilderStateMachine {
    var state = BuilderState.SCOPE
    var context = XContext()
    var cur = 0
    var accum = StringBuilder()

    /**
     * 根据谓语字符串初步生成谓语体
     *
     * @param pre
     * @return
     */
    fun genPredicate(pre: String): Predicate {
        val op = StringBuilder()
        val left = StringBuilder()
        val right = StringBuilder()
        val predicate = Predicate()
        val preArray = pre.toCharArray()
        var index = preArray.size - 1
        var argDeep = 0
        var opFlag = 0

        if (pre.matches( Regex(".+(\\+|=|-|>|<|>=|<=|^=|\\*=|$=|~=|!=|!~)\\s*'.+'"))) {
            while (index >= 0) {
                val tmp = preArray[index]
                if (tmp == '\'') {
                    argDeep += 1
                }
                if (argDeep == 1 && tmp != '\'') {
                    right.insert(0, tmp)
                } else if (argDeep == 2 && instance.commOpChar.contains(tmp)) {
                    op.insert(0, tmp)
                    opFlag = 1
                } else if (argDeep >= 2 && opFlag > 0) {
                    argDeep++ //取完操作符后剩下的都属于left
                    left.insert(0, tmp)
                }
                index -= 1
            }
        } else if (pre.matches(Regex(".+(\\+|=|-|>|<|>=|<=|^=|\\*=|$=|~=|!=|!~)[^']+"))) {
            while (index >= 0) {
                val tmp = preArray[index]
                if (opFlag == 0 && instance.commOpChar.contains(tmp)) {
                    op.insert(0, tmp)
                } else {
                    if (op.length > 0) {
                        left.insert(0, tmp)
                        opFlag = 1
                    } else {
                        right.insert(0, tmp)
                    }
                }
                index -= 1
            }
        }
        predicate.opEm = instance.opEmMap[op.toString()]
        predicate.left = left.toString().trim { it <= ' ' }
        predicate.right = right.toString().trim { it <= ' ' }
        predicate.value = pre
        return predicate
    }

    enum class BuilderState {
        SCOPE {
            override fun parser(stateMachine: NodeTreeBuilderStateMachine, xpath: CharArray) {
                if (stateMachine.cur >= xpath.size) {
                    stateMachine.state = END
                }
                while (stateMachine.cur < xpath.size) {
                    if (!(xpath[stateMachine.cur] == '/' || xpath[stateMachine.cur] == '.')) {
                        stateMachine.state = AXIS
                        val xn = Node()
                        stateMachine.context.xpathTr!!.add(xn)
                        xn.scopeEm = instance.scopeEmMap[stateMachine.accum.toString()]
                        stateMachine.accum = StringBuilder()
                        break
                    }
                    stateMachine.accum.append(xpath[stateMachine.cur])
                    stateMachine.cur += 1
                }
            }
        },
        AXIS {
            override fun parser(stateMachine: NodeTreeBuilderStateMachine, xpath: CharArray) {
                var curtmp = stateMachine.cur
                val accumTmp = StringBuilder()
                while (curtmp < xpath.size && xpath[curtmp] != '[' && xpath[curtmp] != '/') {
                    if (xpath[curtmp] == ':') {
                        stateMachine.context.xpathTr!!.last.axis = accumTmp.toString()
                        stateMachine.cur = curtmp + 2
                        stateMachine.state = TAG
                        break
                    }
                    accumTmp.append(xpath[curtmp])
                    curtmp += 1
                }
                stateMachine.state = TAG
            }
        },
        TAG {
            override fun parser(stateMachine: NodeTreeBuilderStateMachine, xpath: CharArray) {
                while (stateMachine.cur < xpath.size && xpath[stateMachine.cur] != '[' && xpath[stateMachine.cur] != '/') {
                    stateMachine.accum.append(xpath[stateMachine.cur])
                    stateMachine.cur += 1
                }
                stateMachine.context.xpathTr!!.last.tagName = stateMachine.accum.toString()
                stateMachine.accum = StringBuilder()
                if (stateMachine.cur == xpath.size) {
                    stateMachine.state = END
                } else if (xpath[stateMachine.cur] == '/') {
                    stateMachine.state = SCOPE
                } else if (xpath[stateMachine.cur] == '[') {
                    stateMachine.state = PREDICATE
                }
            }
        },
        PREDICATE {
            override fun parser(stateMachine: NodeTreeBuilderStateMachine, xpath: CharArray) {
                var deep = 0
                stateMachine.cur += 1
                while (!(xpath[stateMachine.cur] == ']' && deep == 0)) {
                    if (xpath[stateMachine.cur] == '[') {
                        deep += 1
                    }
                    if (xpath[stateMachine.cur] == ']') {
                        deep -= 1
                    }
                    stateMachine.accum.append(xpath[stateMachine.cur])
                    stateMachine.cur += 1
                }
                val predicate = stateMachine.genPredicate(stateMachine.accum.toString())
                stateMachine.context.xpathTr!!.last.predicate = predicate
                stateMachine.accum = StringBuilder()
                if (stateMachine.cur < xpath.size - 1) {
                    stateMachine.cur += 1
                    stateMachine.state = SCOPE
                } else {
                    stateMachine.state = END
                }
            }
        },
        END {
            override fun parser(stateMachine: NodeTreeBuilderStateMachine, xpath: CharArray) {}
        };

        open fun parser(stateMachine: NodeTreeBuilderStateMachine, xpath: CharArray) {}
    }
}