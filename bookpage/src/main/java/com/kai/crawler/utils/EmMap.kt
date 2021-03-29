package com.kai.crawler.utils

import com.kai.crawler.xpath.model.ScopeEm
import java.util.*

class EmMap private constructor() {
    @JvmField
    var scopeEmMap: MutableMap<String, ScopeEm> = HashMap()
    @JvmField
    var opEmMap: MutableMap<String, OpEm> = HashMap()
    @JvmField
    var commOpChar: MutableSet<Char> = HashSet()

    companion object {
        val instance = EmMap()
    }

    init {
        scopeEmMap["/"] = ScopeEm.INCHILREN
        scopeEmMap["//"] = ScopeEm.RECURSIVE
        scopeEmMap["./"] = ScopeEm.CUR
        scopeEmMap[".//"] = ScopeEm.CURREC
        opEmMap["+"] = OpEm.PLUS
        opEmMap["-"] = OpEm.MINUS
        opEmMap["="] = OpEm.EQ
        opEmMap["!="] = OpEm.NE
        opEmMap[">"] = OpEm.GT
        opEmMap["<"] = OpEm.LT
        opEmMap[">="] = OpEm.GE
        opEmMap["<="] = OpEm.LE
        opEmMap["^="] = OpEm.STARTWITH
        opEmMap["$="] = OpEm.ENDWITH
        opEmMap["*="] = OpEm.CONTAIN
        opEmMap["~="] = OpEm.REGEX
        opEmMap["!~"] = OpEm.NOTMATCH
        commOpChar.add('+')
        commOpChar.add('-')
        commOpChar.add('=')
        commOpChar.add('*')
        commOpChar.add('^')
        commOpChar.add('$')
        commOpChar.add('~')
        commOpChar.add('>')
        commOpChar.add('<')
        commOpChar.add('!')
    }
}