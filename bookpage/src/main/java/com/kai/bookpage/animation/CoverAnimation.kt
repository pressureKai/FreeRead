package com.kai.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlin.math.abs

/**
 *
 * @ProjectName:    bookpage
 * @Description:    翻页动画 - 覆盖
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/20 9:36
 */
class CoverAnimation : BaseHorizontalPageAnimation {
    //当前可绘制范围
    private lateinit var mSrcRect: Rect
    //下个页面可绘制范围
    private lateinit var mDestRect: Rect
    //用于阴影的绘制 DrawableLR
    private lateinit var mBackShadowDrawableLR: GradientDrawable

    /**
     * des 构造函数
     */
    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            marginWidth, marginHeight,
            view, onPageChangeListener) {
        init()
    }

    /**
     * des 构造函数
     */
    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : this(
            screenWidth, screenHeight,
            0, 0,
            view, onPageChangeListener)


    /**
     * des 初始化参数 可绘制范围,以及阴影渐变
     */
    private fun init() {
        mSrcRect = Rect(0, 0, mViewWidth, mViewHeight)
        mDestRect = Rect(0, 0, mViewWidth, mViewHeight)
        //阴影颜色数组
        val mBackShadowColor = intArrayOf(0x66000000, 0x00000000)
        //GradientDrawable 渐变颜色
        mBackShadowDrawableLR = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColor)
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    }

    /**
     * des 绘制静态页面
     * @param canvas 画板
     */
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mCurrentBitmap?.let {
                //静态页面页面被取消时将当前页复制给下一页
                mNextBitmap = it.copy(Bitmap.Config.RGB_565, true)
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        } else {
            mNextBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    /**
     * des 绘制动态页面
     * @param canvas 画板
     */
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