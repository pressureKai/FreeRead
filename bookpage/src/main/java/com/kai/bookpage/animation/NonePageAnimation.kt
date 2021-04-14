package com.kai.bookpage.animation

import android.graphics.Canvas
import android.view.View
import com.kai.common.utils.LogUtils

/**
 *
 * @ProjectName:    bookpage
 * @Description:    翻页动画 - 无
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/20 11:07
 */
class NonePageAnimation :BaseHorizontalPageAnimation {

    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            marginWidth, marginHeight,
            view, onPageChangeListener)

    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : this(
            screenWidth, screenHeight,
            0, 0,
            view, onPageChangeListener)

    /**
     * des 绘制静态页面即不响应触摸事件
     */
    override fun drawStatic(canvas: Canvas) {
        if(isCancel){
            mCurrentBitmap?.let {
                canvas.drawBitmap(it,0f,0f,null)
            }
        }else{
            mNextBitmap?.let {
                canvas.drawBitmap(it,0f,0f,null)
            }
        }
    }

    /**
     * des 绘制动态页面即响应触摸事件
     */
    override fun drawMove(canvas: Canvas) {
        if(isCancel){
            mCurrentBitmap?.let {
                canvas.drawBitmap(it,0f,0f,null)
            }
        }else{
            mNextBitmap?.let {
                canvas.drawBitmap(it,0f,0f,null)
            }
        }
    }
}