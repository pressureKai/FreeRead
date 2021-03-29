package com.kai.common.utils

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
         * 新增增了中文的空格
         * @param str
         * @return
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
    }
}