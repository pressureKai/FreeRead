package com.kai.base.extension

import android.content.Context



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