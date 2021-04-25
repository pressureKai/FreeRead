package com.kai.common.utils

import java.lang.StringBuilder
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
        fun trim(str: String): String {
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
            return if (st > 0 || len < str.length) replaceHeaderSpace(str.substring(st, len)) else replaceHeaderSpace(str)
        }


        private fun replaceHeaderSpace(str: String): String{
            if(str.isEmpty()){
                return str
            }
            val length = str.length
            var isChineseSpace = false
            val toCharArray = str.toCharArray()
            val tempCharArray = CharArray(toCharArray.size)

            for(index in 0.until(length)){
                if(!isChineseSpace){
                    isChineseSpace =  toCharArray[index].toString() == "\\s"
                    if(isChineseSpace){
                        tempCharArray[index] = "".toCharArray().first()
                        isChineseSpace = false
                    } else {
                        tempCharArray[index] = toCharArray[index]
                        isChineseSpace = true
                    }
                } else {
                    tempCharArray[index] = toCharArray[index]
                }
            }

            val replaceBefore = StringBuilder()


            for(value in toCharArray){
                replaceBefore.append(value)
            }
            val replaceAfter = StringBuilder()
            for(value in tempCharArray){
                replaceAfter.append(value)
            }

            //LogUtils.e("StringUtils","replace before is $replaceBefore  replace after is $replaceAfter")

            return replaceAfter.toString()
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