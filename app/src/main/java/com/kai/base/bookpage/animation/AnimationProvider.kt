package com.kai.base.bookpage.animation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.widget.Scroller

abstract class AnimationProvider {
    enum class Direction(val isHorizontal: Boolean) {
        NONE(true),
        NEXT(true),
        PRE(true),
        UP(false),
        DOWN(false);
    }

    private var mCurPageBitmap: Bitmap? = null
    private var mNextPageBitmap : Bitmap ?= null
    private var myStartX :Float = 0F
    private var myStartY :Float = 0F
    protected var myEndX :Float = 0F
    protected var myEndY :Float = 0F
    private var myDirection :Direction = Direction.NONE
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mTouch = PointF()
    private var isCancel = false


    /**
     * 构造方法
     */
    constructor(width: Int,height :Int){
        mScreenWidth = width
        mScreenHeight = height
        mCurPageBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
        mNextPageBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
    }

    fun setStartPoint(x :Float,y :Float){
        myStartX = x
        myStartY = y
    }


    fun setTouchPoint(x :Float,y :Float){
        mTouch.x = x
        mTouch.y = y
    }

    fun setDirection(direction: Direction){
        myDirection = direction
    }

    fun getDirection() :Direction{
        return myDirection
    }


    fun setCancel(isCancel :Boolean){
        this.isCancel = isCancel
    }

    fun getCancel() :Boolean{
        return isCancel
    }

    fun getNextBitmap():Bitmap?{
        return mCurPageBitmap
    }

    fun getBgBitmap() :Bitmap?{
        return mNextPageBitmap
    }


    fun changePage(){
        val bitmap = mCurPageBitmap
        mCurPageBitmap = mNextPageBitmap
        mNextPageBitmap = bitmap
    }
    /**
     * 绘制滑动页面
     */
    abstract fun drawMove()

    /**
     * 绘制静态页面
     */
    abstract fun drawStatic()

    /**
     * 动画绘制
     */
    abstract fun startAnimation(scroller :Scroller)

}