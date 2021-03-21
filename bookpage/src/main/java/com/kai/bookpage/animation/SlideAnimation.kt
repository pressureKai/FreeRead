package com.kai.bookpage.animation

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import kotlin.math.abs

/**
 *
 * @ProjectName:    bookpage
 * @Description:    翻页动画 - 左右滑动
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/20 11:19
 */
class SlideAnimation : BaseHorizontalPageAnimation {
    private lateinit var mSrcRect: Rect
    private lateinit var mDestRect: Rect
    private lateinit var mNextSrcRect: Rect
    private lateinit var mNextDestRect: Rect


    /**
     * des 构造方法
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
     * des 构造方法
     */
    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : this(
            screenWidth, screenHeight,
            0, 0,
            view, onPageChangeListener)

    /**
     * des 初始化相关可绘制区域
     */
    private fun init() {
        mSrcRect = Rect(0, 0, mViewWidth, mViewHeight)
        mDestRect = Rect(0, 0, mViewWidth, mViewHeight)
        mNextSrcRect = Rect(0, 0, mViewWidth, mViewHeight)
        mNextDestRect = Rect(0, 0, mViewWidth, mViewHeight)
    }


    /**
     * des 绘制静态页面
     */
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mCurrentBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        } else {
            mNextBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    /**
     * des 绘制响应事件的动态方法
     */
    override fun drawMove(canvas: Canvas) {
        //distance用于计算前后两个页面各自显示的区域
        var distance = 0
        //判断页面翻转的方向（在HorizontalPageAnimation 中已处理触摸事件判断了页面的翻转方向）
        when (mDirection) {
            Direction.NEXT -> {
                //从右往左滑动
                //页面宽度
                // - 单次触摸事件开始位置
                // + 单次触摸事件在触发MotionEvent.ACTION_UP事件触摸的x坐标 = distance
                // 右边区域显示位置 （下一页应显示的位置）
                distance = (mViewWidth - mStartX + mTouchX).toInt()
                if (distance > mViewWidth) {
                    distance = mViewWidth
                }

                //计算bitmap截取的区域
                mSrcRect.left = mViewWidth - distance
                //计算bitmap在canvas显示的区域
                mDestRect.right = distance
                //计算下一页Bitmap截取的区域
                mNextSrcRect.right = mViewWidth - distance
                //计算下一bitmap在canvas显示的区域
                mNextDestRect.left = distance

                //绘制下一页
                mNextBitmap?.let {
                    canvas.drawBitmap(it, mNextSrcRect, mNextDestRect, null)
                }

                //绘制当前页
                mCurrentBitmap?.let {
                    canvas.drawBitmap(it, mSrcRect, mDestRect, null)
                }
            }
            else -> {
                //从右往左滑动（向前翻页）
                distance = (mTouchX - mStartX).toInt()
                if (distance < 0) {
                    distance = 0
                    mStartX = mTouchX
                }
                mSrcRect.left = mScreenWidth - distance
                mDestRect.right = distance

                mNextSrcRect.right = mScreenWidth - distance
                mNextDestRect.left = distance

                mCurrentBitmap?.let {
                    canvas.drawBitmap(it, mNextSrcRect, mNextDestRect, null)
                }

                mNextBitmap?.let {
                    canvas.drawBitmap(it, mSrcRect, mDestRect, null)
                }

            }
        }
    }


    override fun startAnimation() {
        super.startAnimation()
        var distanceX = 0
        when (mDirection) {
            Direction.NEXT -> {
                if (isCancel) {
                    var distance = (mScreenWidth - mStartX + mTouchX).toInt()
                    if(distance > mScreenWidth){
                        distance = mScreenWidth
                    }

                    distanceX = mScreenWidth - distance
                } else {

                    distanceX = -(mScreenWidth - mStartX + mTouchX).toInt()
                }
            }
            else -> {
                distanceX = if(isCancel){
                    - (abs(mTouchX - mStartX)).toInt()
                }else{
                    (mScreenWidth -(mTouchX - mStartX)).toInt()
                }
            }
        }
        // 指定滑动时间与滑动的距离呈现一个线性关系（滑动的时间与距离成正比）
        val duration = (400 * abs(distanceX)) / mScreenWidth
        mScroller.startScroll(mStartX.toInt(),0,distanceX,0,duration)
    }


}