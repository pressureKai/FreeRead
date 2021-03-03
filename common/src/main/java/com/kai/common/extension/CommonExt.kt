package com.kai.common.extension

import android.view.View
import android.view.ViewGroup
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 * @ProjectName:    My Application
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/13 9:46
 */


fun View.measureView() :IntArray{
    var lp: ViewGroup.LayoutParams = layoutParams
    if (lp == null) {
        lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    val widthSpec = ViewGroup.getChildMeasureSpec(0, 0, lp.width)
    val lpHeight = lp.height
    val heightSpec: Int
    heightSpec = if (lpHeight > 0) {
        View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY)
    } else {
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }
    measure(widthSpec, heightSpec)
    return intArrayOf(measuredWidth, measuredHeight)
}


fun View.getLocation() :IntArray{
    val location = IntArray(2)
    getLocationOnScreen(location)
    return location
}

fun String.isContainChinese(string: String) :Boolean{
    val p: Pattern = Pattern.compile("[\u4e00-\u9fa5]")
    val m: Matcher = p.matcher(string)
    return m.find()
}