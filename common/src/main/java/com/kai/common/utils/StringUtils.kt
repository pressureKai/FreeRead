package com.kai.common.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/29 14:40
 */
class StringUtils {
    companion object{
        /**
         * # 去除空格，包含中文空格
         * @param [str] 待去空格字符串
         * @return 去空格后字符串
         */
        fun trim(str: String): String? {
            if (str.isEmpty()) {
                return ""
            }
            var len = str.length
            var st = 0
            while (st < len && (str[st] <= ' ' || str[st] == '　')) {
                st++
            }
            while (st < len && (str[len - 1] <= ' ' || str[st] == '　')) {
                len--
            }
            return if (st > 0 || len < str.length) str.substring(st, len) else str
        }

        /**
         * # 检验字符串是否为手机格式
         * @param [str] 待检验字符串
         * @return 是否符合格式
         */
        fun verifyPhone(str: String): Boolean{
            val regex = "^1[3456789]\\d{9}$"
            val phone = str.replace(" ", "").trim()
            return if (phone.length !== 11) {
                false
            } else {
                val p: Pattern = Pattern.compile(regex)
                val m: Matcher = p.matcher(phone)
                val isMatch: Boolean = m.matches()
                isMatch
            }
        }
    }
}