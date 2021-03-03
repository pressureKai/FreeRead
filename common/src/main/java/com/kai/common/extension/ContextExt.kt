package com.kai.common.extension

import android.content.Context



fun Context.getScreenWidth():Int{
    return resources.displayMetrics.widthPixels
}


fun Context.getScreenHeight():Int{
    return resources.displayMetrics.heightPixels
}

fun Context.getStatusBarHeight():Int{
    var statusHeight = -1
    try {
        val clazz = Class.forName("com.android.internal.R\$dimen")
        val `object` = clazz.newInstance()
        val height = clazz.getField("status_bar_height")[`object`].toString().toInt()
        statusHeight = resources.getDimensionPixelSize(height)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return statusHeight
}