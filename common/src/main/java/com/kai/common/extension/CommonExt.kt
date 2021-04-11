package com.kai.common.extension

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
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

fun RecyclerView.closeDefaultAnimation(){
    this.itemAnimator?.addDuration = 0
    this.itemAnimator?.changeDuration = 0
    this.itemAnimator?.moveDuration = 0
    this.itemAnimator?.removeDuration = 0
    (this.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
}

fun EditText.formatPhone(start: Int, isAdd: Boolean) {
    val phone = this.text.toString().replace(" ", "").trim()
    val account = phone.length
    val stringBuilder = StringBuilder()
    if(isAdd){
        if(account in 4..7){
            stringBuilder.append(phone.subSequence(0, 3))
            stringBuilder.append(" ")
            stringBuilder.append(phone.subSequence(3, account))
        }else if(account > 7){
            stringBuilder.append(phone.subSequence(0, 3))
            stringBuilder.append(" ")
            stringBuilder.append(phone.subSequence(3, 7))
            stringBuilder.append(" ")
            stringBuilder.append(phone.subSequence(7, account))
        }
    }


    val newString = stringBuilder.toString()
    val oldStringLength = this.text.toString().length
    val newStringLength = newString.length
    if(newString.isNotEmpty()){
        if(this.text.toString() != newString){
            this.setText(newString)
            val newPosition =  start + (newStringLength - oldStringLength) + 1
            this.setSelection(newPosition)
        }
    }
}