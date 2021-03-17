package com.kai.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller

/**
 *
 * @ProjectName:    bookpage
 * @Description:    书本阅读界面动画基类
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/18 14:22
 */
abstract class PageAnimation {
    //正在使用的View
    protected var mView :View ?= null
    //自定义View 控制滑动的对象
    protected lateinit var mScroller :Scroller
    //页面监听
    protected lateinit var onPageChangeListener :OnPageChangeListener
    //手指滑动的方向
    protected var mDirection :Direction = Direction.NONE
    //动画是否在运行
    protected var isRunning  = false
    //屏幕的尺寸
    protected var mScreenWidth = 0
    protected var mScreenHeight = 0
    //屏幕的间距
    protected var mMarginWidth = 0
    protected var mMarginHeight = 0
    //视图的尺寸
    var mViewWidth = 0
    var mViewHeight = 0
    //起始点
    protected var mStartX = 0f
    protected var mStartY = 0f
    //触碰点
    protected var mTouchX = 0f
    protected var mTouchY = 0f
    //上一个触碰点
    protected var mLastTouchX = 0f
    protected var mLastTouchY = 0f

    /**
     * PageAnimation 初始化方法
     */
    private operator fun invoke(
        screenWidth: Int, screenHeight: Int,
        marginWidth: Int, marginHeight: Int,
        view: View,
        onPageChangeListener: OnPageChangeListener
    ){
        mScreenWidth = screenWidth
        mScreenHeight = screenHeight
        mMarginWidth = marginWidth
        mMarginHeight = marginHeight


        mViewWidth = mScreenWidth - (mMarginWidth * 2)
        mViewHeight = mScreenHeight  - (mMarginHeight * 2)

        mView = view
        this.onPageChangeListener = onPageChangeListener

        mScroller = Scroller(mView?.context,LinearInterpolator())
    }


    constructor(
        screenWidth: Int, screenHeight: Int,
        view: View,
        onPageChangeListener: OnPageChangeListener
    ){
         this(screenWidth, screenHeight, 0, 0, view,onPageChangeListener)
    }



    constructor( screenWidth: Int, screenHeight: Int,
                  marginWidth: Int, marginHeight: Int,
                  view: View,
                  onPageChangeListener: OnPageChangeListener){
        this(screenWidth, screenHeight, marginWidth, marginHeight, view,onPageChangeListener)
    }


    /**
     * des 设置动画开始的点
     * @param  x :开始点x轴坐标
     * @param  y :开始点y轴坐标
     */
   open fun setStartPoint(x :Float,y:Float){
        mStartX = x
        mStartY = y

        mLastTouchX = x
        mLastTouchY = y
    }


    /**
     * des 设置触摸点
     * @param  x :触摸点x轴坐标
     * @param  y :触摸点y轴坐标
     */
    open fun setTouchPoint(x:Float,y:Float){
        mLastTouchX = mTouchX
        mLastTouchY = mTouchY

        mTouchX = x
        mTouchY = y
    }

    /**
     * des 开启翻页动画
     */
    open fun startAnimation(){
        if(isRunning){
            return
        }
        isRunning = true
    }


    /**
     * des 设置手指滑动的方向
     * @param direction : 方向枚举类
     */
    open fun setDirection(direction: Direction){
        mDirection = direction
    }

    /**
     * des 获取手指移动的方向
     * @return Direction : 方向枚举类
     */
    fun getDirection() :Direction{
        return mDirection
    }

    /**
     * des 清空当前动画作用的View
     */
    fun clear(){
        mView = null
    }

    /**
     * des 监听触摸事件
     * @param event 触摸事件
     */
    abstract fun onTouchEvent(event :MotionEvent) :Boolean

    abstract fun draw(canvas: Canvas)

    abstract fun scrollAnimation()

    abstract fun abortAnimation()

    abstract fun getBgBitmap() :Bitmap

    abstract fun getNextBitmap():Bitmap

    enum class Direction(val isHorizontal: Boolean) {
        NONE(true),
        NEXT(true),
        PRE(true),
        UP(false),
        DOWN(false);
    }

    public interface OnPageChangeListener{
        fun hasPrePage():Boolean
        fun hasNext():Boolean
        fun pageCancel():Boolean
    }



    @JvmName("isRunning1")
    fun isRunning() : Boolean{
        return isRunning
    }

}