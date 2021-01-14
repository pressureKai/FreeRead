package com.kai.base.bookpage.animation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.widget.Scroller

abstract class AnimationProviders {
    enum class Direction(val isHorizontal: Boolean) {
        NONE(true),
        NEXT(true),
        PRE(true),
        UP(false),
        DOWN(false);
    }

    protected var mCurPageBitmap: Bitmap? = null
    protected var mNextPageBitmap : Bitmap ?= null
    protected var myStartX :Float = 0F
    protected var myStartY :Float = 0F
    protected var myEndX :Float = 0F
    protected var myEndY :Float = 0F
    protected var myDirection :Direction = Direction.NONE
    protected var mScreenWidth = 0
    protected var mScreenHeight = 0
    protected var mPointF = PointF()
    private   var isCancel = false


    /**
     * 构造方法
     */
    constructor(width: Int,height :Int){
        mScreenWidth = width
        mScreenHeight = height
        mCurPageBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
        mNextPageBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
    }




    fun setStartPoint(){

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