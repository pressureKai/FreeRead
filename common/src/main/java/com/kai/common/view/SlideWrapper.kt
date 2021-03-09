package com.kai.common.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

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

    private val mControlView: ArrayList<View> = ArrayList()
    private var mWrapperView: View? = null

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

    /**
     * 处理触摸事件的分发
     * 执行super.dispatchTouchEvent(ev) 事件向下分发
     * 一个完整的触摸事件链有3个组成 MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE,MotionEvent.ACTION_UP
     *  由 ev.getAction() 可知 当前触摸事件是处在哪一个状态
     *
     *  question : 1. 触摸事件是否由上到下逐层传递 (dispatchTouchEvent, onInterceptOnTouchEvent ,onTouchEvent)
     *             2. 传递的对象MotionEvent的不同事件类型会在view的事件传递层中进行怎么样的传递
     *
     *  现象 : onTouchEvent -> true (View成为了事件黑洞,吸收外部所有的事件) 时
     *                                dispatchTouchEvent 每种触摸类型都会调用,
     *                                onInterceptOnTouchEvent 只有在触摸类型 MotionEvent.ACTION_DOWN 中调用一次,
     *
     *
     *         onTouchEvent -> false (不响应任何的触摸事件) 时
     *                                dispatchTouchEvent 和 onInterceptOnTouchEvent 只有在触摸类型 MotionEvent.ACTION_DOWN 中被调用一次
     *                                onTouch在MotionEvent.ACTION_DOWN 中调用一次,返回false表示不处理此次事件,故接下来的事件流并不被此View所接收
     *
     *
     *
     * onInterceptOnTouchEvent -> false : 不拦截事件,事件流会流向子View,如果子View的 onTouchEvent 返回true 或者子View设置了点击事件,
     *                                   原本的 ViewGroup 中的Move,Up事件会继续调用onInterceptOnTouchEvent
     *
     * Activity   dispatchOnTouchEvent(),onTouchEvent()
     * ViewGroup  dispatchOnTouchEvent(),onInterceptOnTouchEvent(),onTouchEvent()
     * View       dispatchOnTouchEvent(),onTouchEvent()
     *                                                                                             ^ false >>>>>  onTouchEvent()[Activity]
     *                                                                                             ^
     * (Activity 中的事件分发)                                                                       ^
     * (步骤一)  MotionEvent.ACTION_DOWN -> Activity -> dispatchOnTouchEvent()[Activity]   -----决定是否分发----- true -> 步骤二
     *
     *
     *
     *                                                                                             ^ false >>>>>> ViewGroup不处理事件
     *                                                                                             ^            (返回至上层Activity的onTouchEvent)
     *                                                                                             ^
     * (ViewGroup 中的事件分发到 onInterceptOnTouchEvent 在 dispatchOnTouchEvent() 之后)               ^
     * (步骤二)  MotionEvent.ACTION_DOWN -> ViewGroup -> dispatchOnTouchEvent()[ViewGroup] -----决定是否分发 ----- true -> onInterceptOnTouchEvent()[ViewGroup]
     *
     *
     *
     *
     * (步骤三) MotionEvent.ACTION_DOWN -> onInterceptOnTouchEvent()[ViewGroup] -> -----决定是否拦截----- true ->
     * @param ev
     * @return false  : 不继续向下分发事件 (代表此控件不处理点击事件)
     *         true  : 向下分发事件由 touchEvent 继续处理
     *
     *         触摸事件向下分发时
     *         向下分发情况有多种,可分为两种具体情况进行分析
     *         1.onTouchEvent -> false 此控件不响应触摸事件
     *
     *         2.onTouchEvent -> true 此控件响应触摸事件
     *
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

    }

    /**
     * 利用tag(ctrl)识别出侧滑后出现的View
     */
    private fun findControlView(): List<View>{
        val controlView = ArrayList<View>()
        for(i in 0.until(childCount)){
            val child = getChildAt(i)
            if(child != null){
                val tag = child.tag
                if(tag != null && tag == CONTROL_TAG){
                     controlView.add(child)
                }
            }
        }
        return controlView
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