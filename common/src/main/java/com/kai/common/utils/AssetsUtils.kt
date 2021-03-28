package com.kai.common.utils

import android.content.Context
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by yuyuhang on 2018/1/8.
 */
object AssetsUtils {
    fun readAssetsTxt(context: Context, fileName: String?): String {
        try {
            val `is` = context.assets.open(fileName!!)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            return String(buffer, Charset.forName("utf-8"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


}