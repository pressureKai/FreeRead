package com.kai.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {
    private var isCanScroll = false
    private var actionListener: ActionListener? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
    }

    override fun callOnClick(): Boolean {
        return super.callOnClick()
    }

    override fun setOnContextClickListener(l: OnContextClickListener?) {
        super.setOnContextClickListener(l)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item, false)
    }

    override fun onInterceptTouchEvent(arg0: MotionEvent): Boolean {
        return if (isCanScroll) {
            super.onInterceptTouchEvent(arg0)
        } else {
            false
        }
    }

    fun setCanScroll(isCanScroll: Boolean) {
        this.isCanScroll = isCanScroll
    }

    fun setActionListener(actionListener: ActionListener?) {
        this.actionListener = actionListener
    }

    interface ActionListener {
        fun actionType(type: Int)
    }
}