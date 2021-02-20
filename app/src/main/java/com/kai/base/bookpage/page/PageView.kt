package com.kai.base.bookpage.page

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.kai.base.bookpage.animation.PageAnimation

class PageView :View{

    private val TAG = "PageView"
    private var mViewWidth = 0
    private var mViewHeight = 0

    private var mStartX = 0
    private var mStartY = 0
    private var isMove = false


    private var mBgColor = 0xFFCEC29C
    private var mPageMode = PageMode.SIMULATION


    private var canTouch = true
    private var mCenterRect : RectF  ?= null
    private var isPrepare  = false

    private var mPageAnimation : PageAnimation ?= null

    private var mPageAnimationListener : PageAnimation.OnPageChangeListener = object :PageAnimation.OnPageChangeListener{
        override fun hasPrePage(): Boolean {
            return this@PageView.hasPrePage()
        }

        override fun hasNext(): Boolean {
            return this@PageView.hasNextPage()
        }

        override fun pageCancel(): Boolean {
            this@PageView.pageCancel()
            return true
        }

    }

    private var mTouchListener : TouchListener ?= null
    private var mPageLoader : PageLoader ?= null



    public fun hasPrePage() :Boolean{
        return false
    }



    public fun hasNextPage():Boolean{
        return false
    }


    public fun pageCancel(){

    }



    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) :
            super(context, attributeSet) {
        init()
    }


    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr) {
        init()
    }

    private fun init(){

    }




    interface TouchListener{
        fun onTouch() :Boolean
        fun center()
        fun prePage()
        fun nextPage()
        fun cancel()
    }

}