package com.kai.common.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

class MD5Utils {
    companion object{
        fun strToMd5By32(str: String): String? {
            var reStr: String? = null
            try {
                val md5 = MessageDigest.getInstance("MD5")
                val bytes = md5.digest(str.toByteArray())
                val stringBuffer = StringBuffer()
                for (b in bytes) {
                    val bt: Int = b.and(0xff.toByte()).toInt()
                    if (bt < 16) {
                        stringBuffer.append(0)
                    }
                    stringBuffer.append(Integer.toHexString(bt))
                }
                reStr = stringBuffer.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return reStr
        }

        fun strToMd5By16(str: String): String? {
            var reStr = strToMd5By32(str)
            if (reStr != null) {
                reStr = reStr.substring(8, 24)
            }
            return reStr
        }
    }
}