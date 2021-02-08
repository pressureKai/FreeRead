package com.kai.base.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller

/**
 *
 * @ProjectName:    My Application
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/18 14:22
 */
abstract class PageAnimation {
    protected var mView :View ?= null
    protected lateinit var mScroller :Scroller
    protected lateinit var onPageChangeListener :OnPageChangeListener
    protected var mDirection :Direction = Direction.NONE
    protected var isRunning  = false

    protected var mScreenWidth = 0
    protected var mScreenHeight = 0

    protected var mMarginWidth = 0
    protected var mMarginHeight = 0

    var mViewWidth = 0
    var mViewHeight = 0

    protected var mStartX = 0f
    protected var mStartY = 0f

    protected var mTouchX = 0f
    protected var mTouchY = 0f

    protected var mLastTouchX = 0f
    protected var mLastTouchY = 0f


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


   open fun setStartPoint(x :Float,y:Float){
        mStartX = x
        mStartY = y

        mLastTouchX = x
        mLastTouchY = y
    }


    open fun setTouchPoint(x:Float,y:Float){
        mLastTouchX = mTouchX
        mLastTouchY = mTouchY

        mTouchX = x
        mTouchY = y
    }



    open fun startAnimation(){
        if(isRunning){
            return
        }
        isRunning = true
    }


    open fun setDirection(direction: Direction){
        mDirection = direction
    }

    fun getDirection() :Direction{
        return mDirection
    }

    fun clear(){
        mView = null
    }

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


}