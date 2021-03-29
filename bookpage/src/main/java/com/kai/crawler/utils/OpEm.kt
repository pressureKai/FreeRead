package com.kai.crawler.utils

import org.apache.commons.lang3.StringUtils

/**
 * 操作符
 */
enum class OpEm(private val `val`: String) {
    PLUS("+") {
        override fun execute(left: String, right: String): Any? {
            var li = 0
            if (StringUtils.isNotBlank(left)) {
                li = left.toInt()
            }
            var ri = 0
            if (StringUtils.isNotBlank(right)) {
                ri = right.toInt()
            }
            return li + ri
        }
    },
    MINUS("-") {
        override fun execute(left: String, right: String): Any? {
            var li = 0
            if (StringUtils.isNotBlank(left)) {
                li = left.toInt()
            }
            var ri = 0
            if (StringUtils.isNotBlank(right)) {
                ri = right.toInt()
            }
            return li - ri
        }
    },
    EQ("=") {
        override fun execute(left: String, right: String): Any? {
            return left == right
        }
    },
    NE("!=") {
        override fun execute(left: String, right: String): Any? {
            return left != right
        }
    },
    GT(">") {
        override fun execute(left: String, right: String): Any? {
            var li = 0
            if (StringUtils.isNotBlank(left)) {
                li = left.toInt()
            }
            var ri = 0
            if (StringUtils.isNotBlank(right)) {
                ri = right.toInt()
            }
            return li > ri
        }
    },
    LT("<") {
        override fun execute(left: String, right: String): Any? {
            var li = 0
            if (StringUtils.isNotBlank(left)) {
                li = left.toInt()
            }
            var ri = 0
            if (StringUtils.isNotBlank(right)) {
                ri = right.toInt()
            }
            return li < ri
        }
    },
    GE(">=") {
        override fun execute(left: String, right: String): Any? {
            var li = 0
            if (StringUtils.isNotBlank(left)) {
                li = left.toInt()
            }
            var ri = 0
            if (StringUtils.isNotBlank(right)) {
                ri = right.toInt()
            }
            return li >= ri
        }
    },
    LE("<=") {
        override fun execute(left: String, right: String): Any? {
            var li = 0
            if (StringUtils.isNotBlank(left)) {
                li = left.toInt()
            }
            var ri = 0
            if (StringUtils.isNotBlank(right)) {
                ri = right.toInt()
            }
            return li <= ri
        }
    },
    STARTWITH("^=") {
        override fun execute(left: String, right: String): Any? {
            return left.startsWith(right)
        }
    },
    ENDWITH("$=") {
        override fun execute(left: String, right: String): Any? {
            return left.endsWith(right)
        }
    },
    CONTAIN("*=") {
        override fun execute(left: String, right: String): Any? {
            return left.contains(right)
        }
    },
    REGEX("~=") {
        override fun execute(left: String, right: String): Any? {
            return left.matches(Regex(right))
        }
    },
    NOTMATCH("!~") {
        override fun execute(left: String, right: String): Any? {
            return !left.matches(Regex(right))
        }
    };

    fun `val`(): String {
        return `val`
    }

    open fun execute(left: String, right: String): Any? {
        return null
    }
}