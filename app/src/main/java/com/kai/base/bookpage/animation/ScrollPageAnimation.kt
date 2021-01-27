package com.kai.base.bookpage.animation

import android.graphics.Canvas
import android.view.View

/**
 *
 * @ProjectName:    My Application
 * @Description:    翻页动画-上下滑动
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/27 10:09
 */
class ScrollPageAnimation :BasePageAnimation{

    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            marginWidth, marginHeight,
            view, onPageChangeListener) {
        init()
    }

    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            0, 0,
            view, onPageChangeListener) {
        init()
    }

    private fun init(){

    }

    override fun drawStatic(canvas: Canvas) {

    }

    override fun drawMove(canvas: Canvas) {

    }
}