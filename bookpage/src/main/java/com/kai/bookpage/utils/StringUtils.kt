package com.kai.bookpage.utils

import android.content.Context
import android.graphics.Paint
import android.util.Log
import androidx.annotation.StringRes
import com.kai.bookpage.page.ReadSettingManager
import com.kai.common.application.BaseApplication
import com.kai.common.utils.LogUtils
import com.kai.common.utils.SharedPreferenceUtils
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/4 17:27
 */
class StringUtils {
    companion object {
        val HOUR_OF_DAY = 24
        val DAY_OF_YESTERDAY = 2
        val TIME_UNIT = 60

        /**
         * 繁简转换
         */
        fun convertCC(input: String, context: Context): String {
            var currentConversionType = ConversionType.S2TW
            val convertType = SharedPreferenceUtils.getInstance()?.getInt(ReadSettingManager.SHARED_READ_CONVERT_TYPE, 0)
            if (input.isEmpty()) {
                return ""
            }

            convertType?.let {
                when (it) {
                    1 -> {
                        currentConversionType = ConversionType.TW2SP
                    }
                    2 -> {
                        currentConversionType = ConversionType.S2HK
                    }
                    3 -> {
                        currentConversionType = ConversionType.S2T
                    }
                    4 -> {
                        currentConversionType = ConversionType.S2TW
                    }
                    5 -> {
                        currentConversionType = ConversionType.S2TWP
                    }
                    6 -> {
                        currentConversionType = ConversionType.T2HK
                    }
                    7 -> {
                        currentConversionType = ConversionType.T2S
                    }
                    8 -> {
                        currentConversionType = ConversionType.T2TW
                    }
                    9 -> {
                        currentConversionType = ConversionType.TW2S
                    }
                    10 -> {
                        currentConversionType = ConversionType.HK2S
                    }
                }
            }

            return if (convertType != 0) {
                ChineseConverter.convert(input, currentConversionType, context)
            } else {
                input
            }
        }

        /**
         * 将文本中的半角字符,转换成全角字符
         */
        fun halfToFull(input: String): String {
            val c = input.toCharArray()
            for ((index, value) in c.withIndex()) {
                //半角空格
                if (value.toInt() == 32) {
                    c[index] = 12288.toChar()
                    continue
                }
                //根据实际情况，过滤不需要转换的符号
                //if(c[index].toInt() == 46) 半角点号不转换
                //其他字符都转换为全角
                if (c[index].toInt() in 33..126) {
                    c[index] = (c[index].toInt() + 65248).toChar()
                }
            }
            return String(c)
        }

        /**
         * 字符串全角转换为半角
         */
        fun fullToHalf(input: String): String {
            val c = input.toCharArray()
            for ((index, value) in c.withIndex()) {
                //全角空格
                if (value.toInt() == 12288) {
                    c[index] = 32.toChar()
                    continue
                }

                if (c[index].toInt() in 65281..65374) {
                    c[index] = (c[index] - 65248).toChar()
                }
            }
            return String(c)
        }

        /**
         * 根据字符串资源id  获取字符串
         */
        fun getString(@StringRes id: Int): String {
            var resourceString = ""
            BaseApplication.getContext()?.let {
                try {
                    resourceString = it.resources.getString(id)
                } catch (e: Exception) {
                    Log.e("StringUtils", "getString error is $e")
                }
            }
            return resourceString
        }


        /**
         * 根据指定格式获取与字符串资源id 获取字符串
         */
        fun getString(@StringRes id: Int, vararg formatArgs: Any): String {
            var resourceString = ""
            BaseApplication.getContext()?.let {
                try {
                    it.resources.getString(id, formatArgs)
                } catch (e: Exception) {
                    Log.e("StringUtils", "getString format error is $e")
                }
            }
            return resourceString
        }


        /**
         * 将时间转换成日期
         */
        fun dateConvert(time: Long, pattern: String): String {
            val date = Date(time)
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(date)
        }


        /**
         * 将日期转换成昨天和今天以及明天
         */
        fun dateConvert(source: String, pattern: String): String {
            val simpleDateFormat = SimpleDateFormat(pattern)
            val calendar = Calendar.getInstance()
            try {
                val date = simpleDateFormat.parse(source)
                val curTime = calendar.timeInMillis
                calendar.time = date

                //秒
                val differentSecond = abs((curTime - date.time) / 1000).toLong()
                val differentMinute = (differentSecond / 60).toLong()
                val differentHour = (differentMinute / 60).toLong()
                val differentDate = (differentHour / 60).toLong()

                val oldHour = calendar.get(Calendar.HOUR)
                //如果没有时间
                if (oldHour == 0) {
                    //比日期:昨天和今天以及明天
                    return when {
                        differentDate == 0L -> {
                            "今天"
                        }
                        differentDate < DAY_OF_YESTERDAY -> {
                            "昨天"
                        }
                        else -> {
                            val convertFormat = SimpleDateFormat("yyyy-MM-dd")
                            convertFormat.format(date)
                        }
                    }
                }


                when {
                    differentSecond < TIME_UNIT -> {
                        return differentSecond.toString() + "秒前"
                    }
                    differentMinute < TIME_UNIT -> {
                        return differentMinute.toString() + "分钟前"
                    }
                    differentHour < HOUR_OF_DAY -> {
                        return differentHour.toString() + "小时前"
                    }
                    differentDate < DAY_OF_YESTERDAY -> {
                        return "昨天"
                    }
                    else -> {
                        val convertFormat = SimpleDateFormat("yyyy-MM-dd")
                        return convertFormat.format(date)
                    }
                }
            } catch (e: java.lang.Exception) {
                LogUtils.e("StringUtils", e.toString())
            }
            return ""
        }


        /**
         * unConfirm
         */
        fun toFirstCapital(input: String): String {
            var upperCaseString = input
            try {
                input.substring(0, 1).toUpperCase() + input.substring(1)
            } catch (e: java.lang.Exception) {
                LogUtils.e("StringUtils", e.toString())
            }
            return upperCaseString
        }



        fun getWordCount(input: String,paint: Paint,width :Float) :Int{
            var wordCount  = 0
            var beMeasureString = input
            try {
                if(input.contains("fi")){
                    beMeasureString = beMeasureString.replace("fi","_!")
                }
                wordCount = paint.breakText(beMeasureString,true,width, null)
            }catch (e: java.lang.Exception){
                LogUtils.e("StringUtils", e.toString())
            }
            return wordCount
        }
    }




}