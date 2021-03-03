package com.kai.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlin.math.abs

/**
 *
 * @ProjectName:    My Application
 * @Description:    翻页动画 - 覆盖
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/20 9:36
 */
class CoverAnimation : BasePageAnimation {
    private lateinit var mSrcRect: Rect
    private lateinit var mDestRect: Rect
    private lateinit var mBackShadowDrawableLR: GradientDrawable

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


    private fun init() {
        mSrcRect = Rect(0, 0, mViewWidth, mViewHeight)
        mDestRect = Rect(0, 0, mViewWidth, mViewHeight)
        val mBackShadowColor = intArrayOf(0x66000000, 0x00000000)
        mBackShadowDrawableLR = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColor)
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    }

    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mCurrentBitmap?.let {
                mNextBitmap = it.copy(Bitmap.Config.RGB_565, true)
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        } else {
            mNextBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    override fun drawMove(canvas: Canvas) {
        when (mDirection) {
            Direction.NEXT -> {
                var distance = (mViewWidth - mStartX + mTouchX).toInt()
                if (distance > mViewWidth) {
                    distance = mViewWidth
                }

                //向下翻页,计算当前页面随手指移动时显示的部分
                mSrcRect.left = mViewWidth - distance
                mDestRect.right = distance

                mNextBitmap?.let {
                    canvas.drawBitmap(it, 0f, 0f, null)
                }

                mCurrentBitmap?.let {
                    canvas.drawBitmap(it, mSrcRect, mDestRect, null)
                }
                addShadow(distance, canvas)
            }
            else -> {
                mSrcRect.left = (mViewWidth - mTouchX).toInt()
                mDestRect.right = mTouchX.toInt()

                mCurrentBitmap?.let {
                    canvas.drawBitmap(it, 0f, 0f, null)
                }

                mNextBitmap?.let {
                    canvas.drawBitmap(it, mSrcRect, mDestRect, null)
                }
                addShadow(mTouchX.toInt(), canvas)
            }
        }
    }

    /**
     * 增加阴影
     */
    private fun addShadow(left: Int, canvas: Canvas) {
        //mScreenHeight 在有边距的情况下会不会过长
        mBackShadowDrawableLR.setBounds(left, 0, left + 30, mScreenHeight)
        mBackShadowDrawableLR.draw(canvas)
    }

    override fun startAnimation() {
        super.startAnimation()
        //滑动的距离
        var distanceAnimation = 0
        when (mDirection) {
            Direction.NEXT -> {
                if (isCancel) {
                    var distance = (mScreenWidth - mStartX + mTouchX).toInt()
                    if (distance > mScreenWidth) {
                        distance = mScreenWidth
                    }
                    distanceAnimation = mScreenWidth - distance
                } else {
                    distanceAnimation = -(mScreenWidth - mStartX + mTouchX).toInt()
                }
            }
            else -> {
                distanceAnimation = if (isCancel) {
                    -mTouchX.toInt()
                } else {
                    (mScreenWidth - mTouchX).toInt()
                }
            }
        }

        val duration = (abs(distanceAnimation) * 400) / mScreenWidth
        mScroller.startScroll(mTouchX.toInt(), 0, distanceAnimation, 0, duration)
    }
}