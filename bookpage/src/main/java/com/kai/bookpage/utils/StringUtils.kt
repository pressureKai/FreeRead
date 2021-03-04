package com.kai.bookpage.utils

import android.content.Context
import com.kai.bookpage.page.ReadSettingManager
import com.kai.common.utils.SharedPreferenceUtils
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/4 17:27
 */
class StringUtils {
    companion object {
        //繁简转换
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

            return if(convertType != 0){
                ChineseConverter.convert(input,currentConversionType,context)
            } else {
                input
            }
        }
    }
}