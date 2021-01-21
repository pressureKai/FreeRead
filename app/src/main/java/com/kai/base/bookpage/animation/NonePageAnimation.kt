package com.kai.base.bookpage.animation

import android.graphics.Canvas
import android.view.View

/**
 *
 * @ProjectName:    My Application
 * @Description:    翻页动画 - 无
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/20 11:07
 */
class NonePageAnimation :BasePageAnimation {
    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            marginWidth, marginHeight,
            view, onPageChangeListener) {}
    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            0, 0,
            view, onPageChangeListener) {}

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