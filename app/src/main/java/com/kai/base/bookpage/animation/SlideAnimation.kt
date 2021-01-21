package com.kai.base.bookpage.animation

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View

/**
 *
 * @ProjectName:    My Application
 * @Description:    翻页动画 - 左右滑动
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/20 11:19
 */
class SlideAnimation : BasePageAnimation{
    private lateinit var mSrcRect: Rect
    private lateinit var mDestRect :Rect
    private lateinit var mNextSrcRect :Rect
    private lateinit var mNextDestRect: Rect


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
        mSrcRect = Rect(0,0,mViewWidth,mViewHeight)
        mDestRect = Rect(0,0,mViewWidth,mViewHeight)
        mNextSrcRect = Rect(0,0,mViewWidth,mViewHeight)
        mNextDestRect = Rect(0,0,mViewWidth,mViewHeight)
    }


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
        var distance = 0
        when(mDirection){
            Direction.NEXT ->{
                distance = (mViewWidth - mStartX + mTouchX).toInt()
                if(distance > mViewWidth){
                    distance = mViewWidth
                }

                mSrcRect.left = mViewWidth - distance
                mDestRect.right = distance

                mNextSrcRect.right = mViewWidth -distance
                mNextDestRect.left = distance

                mNextBitmap?.let {
                    canvas.drawBitmap(it,mNextSrcRect,mNextDestRect,null)
                }

                mCurrentBitmap?.let {
                    canvas.drawBitmap(it,mSrcRect,mDestRect,null)
                }
            }
            else ->{
                distance = (mTouchX -mStartX).toInt()

                if(distance < 0){
                    distance = 0
                    mStartX = mTouchX
                }


                mSrcRect.left = mScreenWidth - distance

            }
        }
    }



}