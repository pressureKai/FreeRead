package com.kai.common.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import android.widget.Scroller
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:    侧滑包装器
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/9 11:02
 */
class SlideWrapper : RelativeLayout {
    companion object {
        fun dip2px(context: Context, dipValue: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dipValue * density + 0.5f).toInt()
        }
    }

    val TAG = "SlideWrapper"
    val CONTROL_TAG = "ctrl"
    val CONFIRM_TAG = "confirm"
    val ANIMATION_DURATION = 300
    val CLICK_INTERVAL = 300;
    val CLICK_DISTANCE = 50
    val LONG_CLICK_INTERVAL = ViewConfiguration.getLongPressTimeout()
    val TAN60 = 1.73f

    var enableSlide = true

    private val mControlView: ArrayList<View> = ArrayList()
    private var mWrapperView: View? = null
    //是否已经确定了阻止父控件拦截触摸事件,提升效率用的标记避免频繁在dispatchTouchEvent中做浮点运算
    private var mIsDecided = false
    private var mScroller: Scroller ?= null
    private var mNotifyCompleteOpen =  false
    private var mNotifyCompleteClose = false
    private var mNotifyPullback = false
    private var mCallback :Callback ?= null
    private var mAnimationDurationMs = ANIMATION_DURATION
    private var mCloseOnClick = true
    private var mEnable = true
    //按下时是否处于展开状态
    private var mIsOpenWhenTouchDown = false
    //以下用于辅助判断长按和点击事件
    private var mDownTime = 0L
    private var mUpTime = 0L
    //不触发点击、长按(在展开的状态下点击时不触发)
    private var doNotPerformClick = false

    private var mX = 0f
    private var mY = 0f

    private var mXDown = 0f
    private var mYDown = 0f
    private var mXMove = 0f
    private var mYMove = 0f
    private var mXLastMove = 0f
    private var mYLastMove = 0f
    private var mScrollable = -1
    //Runnable 包裹的View 的长按事件
    private val mLongPressRunnable: Runnable = Runnable { performClick() }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(attrs)
    }


    private fun init(attrs: AttributeSet?) {


    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if(enableSlide){
            var deltaX = 0
            var deltaY = 0
            ev?.let {
                when(it.action){
                   MotionEvent.ACTION_DOWN ->{
                       mIsDecided = false
                       mIsOpenWhenTouchDown = false
                       mXDown = it.x
                       mYDown = it.y
                       mX = mXDown
                       mY = mYDown
                       if(!doNotPerformClick){
                            //在最开始分发事件时，设置对LongClickListener的回调
                            postDelayed(mLongPressRunnable,LONG_CLICK_INTERVAL.toLong())
                       }
                   }
                   MotionEvent.ACTION_MOVE ->{
                       if(abs(it.x - mX) > CLICK_DISTANCE &&
                               abs(it.y - mY) > CLICK_DISTANCE &&
                               abs(it.y - mY) < abs(it.x - mX)){
                           removeCallbacks(mLongPressRunnable)
                       }
                       if(!mIsDecided){
                           deltaX = abs(it.x - mXDown).toInt()
                           deltaY = abs(it.y - mYDown).toInt()

                           if(deltaX > 0 && deltaY > 0 && deltaX > deltaY){
                               mIsDecided = true
                               val tangle = (deltaX / deltaY).toFloat()
                               if(tangle > TAN60){
                                   //当角度值超过60度时由此View接管触摸事件，同时禁用点击事件与长按事件
                                   doNotPerformClick = true
                                   removeCallbacks(mLongPressRunnable)
                                   //让此控件拦截事件,子控件接收不到事件的分发
                                   requestDisallowInterceptTouchEvent(true)
                               }
                           }
                       }
                   }
                   MotionEvent.ACTION_CANCEL,MotionEvent.ACTION_UP ->{
                        removeCallbacks(mLongPressRunnable)
                        requestDisallowInterceptTouchEvent(false)
                        if(mIsOpenWhenTouchDown && mCloseOnClick){
                            close()
                        }
                   }
                }
            }
            if(mScrollable == -1){
                mScrollable = calculateScrollableDistance()
            }

            super.dispatchTouchEvent(ev)
        } else {
            false
        }
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(isOpen()){
            mIsOpenWhenTouchDown = true
            //不拦截,将事件传递给子View
            return false
        }
        ev?.let {
            when(it.action){
                MotionEvent.ACTION_DOWN -> {
                    mXDown = it.x
                    mYDown = it.y
                    mXLastMove = mXDown
                    mYLastMove = mYDown
                }
                MotionEvent.ACTION_MOVE -> {
                    mXMove = it.x
                    mYMove = it.y
                    mXLastMove = mXMove
                    mYLastMove = mYMove
                }
                MotionEvent.ACTION_CANCEL ->{
                    mXMove = 0f
                    mYMove = 0f
                    mXLastMove = 0f
                    mYLastMove = 0f
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun isOpen() :Boolean{
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(mWrapperView == null){
            mWrapperView = findContentView()
            if(mWrapperView == null){
                return false
            }
        }
        event?.let {
            when(it.action){
                MotionEvent.ACTION_DOWN -> {
                    mDownTime = System.currentTimeMillis()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    mXMove = it.x
                    mYMove = it.y
                    //计算移动的距离
                    val scrolledX = (mXMove - mXLastMove).roundToInt()
                    val scrolledY = (mYMove - mYLastMove).roundToInt()
                    if(abs(scrolledX) > abs(scrolledY)
                            && abs(scrolledY) > CLICK_DISTANCE){
                        doAnimation()
                        parent.requestDisallowInterceptTouchEvent(false)
                    }else{
                        handleMotionEvent(scrolledX)
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    mXLastMove = mXMove
                    mYLastMove = mYMove
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    mUpTime = System.currentTimeMillis()
                    if(mUpTime - mDownTime <= CLICK_INTERVAL &&
                            abs(it.x - mX) < CLICK_DISTANCE &&
                            abs(it.y - mY) < CLICK_DISTANCE){
                        if(scrollX > CLICK_DISTANCE){
                            strongClose()
                        }else{
                            performClick()
                        }
                    }
                    doAnimation()
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    strongClose()
                    return true
                }
                else -> {

                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun strongClose(){

    }

    private fun doAnimation(){

    }


    private fun handleMotionEvent(scrolledX :Int){

    }


    private fun calculateScrollableDistance(): Int{
        return -1
    }

    private fun close(){

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        resetControlView(findControlView())
        mWrapperView = findContentView()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec,heightMeasureSpec)
        val contentView = findContentView()
        if(contentView != null){
            val height = contentView.measuredHeight
            val confirmView = findConfirmView()
            confirmView?.let {
                val confirmWidth = it.measuredWidth
                val widthSpec = MeasureSpec.makeMeasureSpec(confirmWidth,MeasureSpec.EXACTLY)
                val heightSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY)
                it.measure(widthSpec,heightSpec)
            }
            if(mControlView.isNotEmpty()){
                for(i in 0.until(mControlView.size)){
                    val controlView = mControlView[i]
                    val measuredWidth = controlView.measuredWidth
                    val widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
                    val heightSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY)
                    controlView.measure(widthSpec,heightSpec)
                }
            }
        }
    }


    //unConfirm
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val contentView = findContentView() ?: return
        val contentViewWidth = contentView.measuredWidth
        val contentViewHeight = contentView.measuredHeight
        contentView.layout(0,0,contentViewWidth,contentViewHeight)
        val confirmView = findConfirmView()
        confirmView?.let {
            it.layout(contentViewWidth,0,width + confirmView.measuredWidth,contentViewHeight)
            it.visibility = View.INVISIBLE
        }
        if(mControlView.isNotEmpty()){
            var left = contentViewWidth
            for(i in 0.until(mControlView.size)){
                val controlView = mControlView[i]
                controlView.layout(left,0,left + controlView.measuredWidth,controlView.measuredHeight)
                left += controlView.measuredWidth
            }
        }
    }



    /**
     * 寻找某些操作时用于二次确认的View
     */
    private fun findConfirmView(): View?{
        for(i in 0.until(childCount)){
            val child = getChildAt(i)
            if(child != null){
                val tag = child.tag
                if(tag != null && tag == CONFIRM_TAG){
                    return child
                }
            }
        }
        return null
    }


    private fun resetControlView(views :List<View>){
        mControlView.clear()
        for(value in views){
            mControlView.add(value)
        }
    }

    /**
     * 利用tag(ctrl)识别出侧滑后出现的View
     */
    private fun findControlView(): List<View> {
        val controlView = ArrayList<View>()
        for (i in 0.until(childCount)) {
            val child = getChildAt(i)
            if (child != null) {
                val tag = child.tag
                if (tag != null && tag == CONTROL_TAG) {
                    controlView.add(child)
                }
            }
        }
        return controlView
    }


    private fun findContentView(): View? {
        for (i in 0.until(childCount)) {
            val child = getChildAt(i)
            if(child != null){
                val tag = child.tag
                if(tag == null || (tag != null && (
                                tag !is String  ||
                                        ( (tag != CONTROL_TAG)
                                                && (tag != CONFIRM_TAG) )
                        ))){
                    return child
                }
            }
        }
        return null
    }


    /**
     * viewFactory 实现例子，侧滑块由若干个TextView组成，默认实现两个
     */

    /**
     * 不在布局中写，运行期动态包装时可见实现该接口提供的侧滑view
     */
    interface ViewFactory {
        /**
         * 生成子View
         *
         * @param context
         */
        fun getView(context: Context): List<View>?


        /**
         * 每个子View的宽度,以dp为单位
         */
        fun getWithDp(): List<Int>?
    }


    interface Callback {
        /**
         * 完全展开时的回调
         */
        fun onCompletelyOpen(wrapper: SlideWrapper)

        /**
         * 从打开状态变成关闭时的回调
         */
        fun onCompleteClosed(wrapper: SlideWrapper)


        /**
         * 拖到中途手势抬起后被拉回的回调
         */
        fun onPullBack(wrapper: SlideWrapper)
    }


    /**
     * 空的实现
     */
    class EmptyViewFactory : ViewFactory {
        override fun getView(context: Context): List<View>? {
            return null
        }

        override fun getWithDp(): List<Int>? {
            return null
        }
    }

    class TextViewFactory : ViewFactory {
        var paddingDp = 8
        override fun getView(context: Context): List<View>? {
            val toTop = TextView(context)
            val delete = TextView(context)
            val views = ArrayList<View>()
            views.add(toTop)
            views.add(delete)
            for ((i, view) in views.withIndex()) {
                val textView = view as TextView
                paddingDp = dip2px(context, paddingDp.toFloat())
                textView.setBackgroundColor(getBackgroundColor()[i])
                textView.setTextColor(getTextColor()[i])
                textView.text = getText()[i]
                textView.textSize =
                        dip2px(context, getTextSize()[i].toFloat()).toFloat()
                textView.gravity = Gravity.CENTER
            }
            return views
        }

        /**
         * 返回每个按钮的背景色
         */
        open fun getBackgroundColor(): List<Int> {
            val bg = ArrayList<Int>()
            bg.add(Color.LTGRAY)
            bg.add(Color.RED)
            return bg
        }

        override fun getWithDp(): List<Int>? {
            val width = ArrayList<Int>()
            width.add(90)
            width.add(80)
            return width
        }

        /**
         * 返回每个按钮的文字
         */
        open fun getText(): List<String> {
            val text = ArrayList<String>()
            text.add("置顶")
            text.add("删除")
            return text
        }

        /**
         * 返回每个按钮文字颜色
         */
        open fun getTextColor(): List<Int> {
            val textColors = ArrayList<Int>()
            textColors.add(Color.WHITE)
            textColors.add(Color.WHITE)
            return textColors
        }


        open fun getTextSize(): List<Int> {
            val textSizes = ArrayList<Int>()
            textSizes.add(8)
            textSizes.add(8)
            return textSizes
        }
    }


}