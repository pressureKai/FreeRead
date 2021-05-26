package com.kai.common.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import android.widget.Scroller
import android.widget.TextView
import com.kai.common.R
import com.kai.common.application.BaseApplication
import java.util.*

/**
 * Created by wangqiang on 16/11/17.
 */
/**
 * Created by wangqiang on 16/11/16.
 * 滑动删除的包装器，任何想拥有侧滑删除功能的view都可以用该包装器来包装
 */
class SlideWrapper : RelativeLayout {
    /**
     * 回调
     */
    interface Callback {
        /**
         * 完全展开时的回调
         *
         * @param wrapper
         */
        fun onCompletelyOpen(wrapper: SlideWrapper?)

        /**
         * 从打开状态变成关闭时的回调
         *
         * @param wrapper
         */
        fun onCompleteClosed(wrapper: SlideWrapper?)

        /**
         * 拖到中途手势抬起后被拉回后的回调
         *
         * @param wrapper
         */
        fun onPullBack(wrapper: SlideWrapper?)
    }

    /**
     * 不在布局中写，运行期动态包装时可实现该接口提供侧滑view
     */
    interface ViewFactory {
        /**
         * 生成子View
         *
         * @param context
         * @return
         */
        fun getView(context: Context): List<View>?

        /**
         * 每个子view的宽度，以dp为单位
         */
        val widthDp: List<Int>?
    }

    fun setCallback(cb: Callback?) {
        mCallback = CallbackWrapper(cb)
    }

    //设置自己的ViewFactory,需要在运行时包装子View时调用
    fun setViewFactory(factory: ViewFactory?) {
        if (factory == null) {
            return
        }
        mFactory = factory
        mControlView = mFactory.getView(context)
        mIsFromFactory = true
        if (mControlView != null) {
            for (v in mControlView!!) {
                addView(v)
            }
        }
    }

    /**
     * 空的实现
     */
    class EmptyViewFactory : ViewFactory {
        override fun getView(context: Context): List<View>? {
            return null
        }

        override val widthDp: List<Int>?
            get() = null
    }

    /**
     * view factory实现例子，侧滑块是若干个TextView，默认实现是2个，
     * 可以重写某些方法自定义
     */
    class TextViewFactory : ViewFactory {
        var paddingDp = 8
        override val widthDp: List<Int>?
            get() {
                val width: MutableList<Int> = ArrayList()
                width.add(90)
                width.add(80)
                return width
            }

        /**
         * 返回每个按钮的文字
         *
         * @return
         */
        protected val text: List<String>
            protected get() {
                val text = ArrayList<String>()
                text.add("置顶")
                text.add("删除")
                return text
            }

        /**
         * 返回每个按钮的背景色
         *
         * @return
         */
        protected val backgroundColor: List<Int>
            protected get() {
                val bg = ArrayList<Int>()
                bg.add(Color.LTGRAY)
                bg.add(Color.RED)
                return bg
            }

        /**
         * 返回每个按钮的文字颜色
         *
         * @return
         */
        protected val textColor: List<Int>
            protected get() {
                val txtClr = ArrayList<Int>()
                txtClr.add(Color.WHITE)
                txtClr.add(Color.WHITE)
                return txtClr
            }

        /**
         * 返回文字大小（dp为单位）
         *
         * @return
         */
        protected val textSize: List<Int>
            protected get() {
                val txtSize = ArrayList<Int>()
                txtSize.add(8)
                txtSize.add(8)
                return txtSize
            }

        override fun getView(context: Context): List<View>? {
            val toTop = TextView(context)
            val delete = TextView(context)
            val views = ArrayList<View>()
            views.add(toTop)
            views.add(delete)
            for (i in views.indices) {
                val tv = views[i] as TextView
                paddingDp = dip2px(context, paddingDp.toFloat())
                tv.setBackgroundColor(backgroundColor[i])
                tv.setTextColor(textColor[i])
                tv.text = text[i]
                tv.textSize = dip2px(context, textSize[i].toFloat()).toFloat()
                tv.gravity = Gravity.CENTER
            }
            return views
        }
    }

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    public override fun onFinishInflate() {
        super.onFinishInflate()
        mControlView = findControlView()
        mWrappedView = findContentView()
        enableControlView(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val c = context
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val contentView = findContentView()
        if (contentView != null) {
            val h = contentView.measuredHeight
            val confirmView = findConfirmView()
            if (confirmView != null) {
                val confirmViewWidth = confirmView.measuredWidth
                val wspec = MeasureSpec.makeMeasureSpec(confirmViewWidth, MeasureSpec.EXACTLY)
                val hspec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
                confirmView.measure(wspec, hspec)
            }
            if (mControlView != null && mControlView!!.size > 0) {
                var controlViewW = 0
                for (i in mControlView!!.indices) {
                    val cv = mControlView!![i]
                    controlViewW = if (mIsFromFactory) dip2px(
                        c, mFactory.widthDp!![i]
                            .toFloat()
                    ) else cv.measuredWidth
                    val wspec = MeasureSpec.makeMeasureSpec(controlViewW, MeasureSpec.EXACTLY)
                    val hspec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
                    cv.measure(wspec, hspec)
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val contentView = findContentView() ?: return
        val width = contentView.measuredWidth
        val height = contentView.measuredHeight
        contentView.layout(0, 0, width, height)
        val confirmView = findConfirmView()
        if (confirmView != null) {
            confirmView.layout(width, 0, width + confirmView.measuredWidth, height)
            confirmView.visibility = INVISIBLE
        }
        if (mControlView != null && mControlView!!.size > 0) {
            if (mMode == MODE_DYNAMIC) {
                var left = width
                for (i in mControlView!!.indices) {
                    val ctrlView = mControlView!![i]
                    ctrlView.layout(left, 0, left + ctrlView.measuredWidth, ctrlView.measuredHeight)
                    left += ctrlView.measuredWidth
                }
            } else {
                var right = width
                for (i in mControlView!!.indices.reversed()) {
                    val ctrlView = mControlView!![i]
                    ctrlView.layout(right - ctrlView.measuredWidth, 0, right, measuredHeight)
                    right -= ctrlView.measuredWidth
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (enableSlide) {
            var deltaX = 0
            var deltaY = 0
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsDecided = false
                    mIsOpenWhenTouchDown = false
                    mXDown = (ev.x).toFloat()
                    mYDown = (ev.y).toFloat()
                    mIsMoved = false
                    mX = mXDown
                    mY = mYDown
                    if (!doNotPerformClick) {
                        postDelayed(mLongPressRunnable, LONG_CLICK_INTERVAL.toLong())
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(ev.x - mX) > CLICK_DISTANCE && Math.abs(ev.y - mY) > CLICK_DISTANCE && Math.abs(
                            ev.y - mY
                        ) < Math.abs(ev.x - mX)
                    ) {
                        mIsMoved = true
                        removeCallbacks(mLongPressRunnable)
                    }
                    if (!mIsDecided) {
                        deltaX = Math.abs(ev.x.toInt() - mXDown.toInt())
                        deltaY = Math.abs(ev.y.toInt() - mYDown.toInt())
                        if (deltaX > 0 && deltaY > 0 && deltaX > deltaY) {
                            mIsDecided = true
                            val tan = deltaX.toFloat() / deltaY
                            if (tan > TAN60) {
                                //当角度值超过60度时就由此View处理触摸事件,同时把点击事件暂时禁用掉
                                doNotPerformClick = true
                                removeCallbacks(mLongPressRunnable)
                                //让此控件拦截事件,子控件接收不到事件的分发
                                requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    removeCallbacks(mLongPressRunnable)
                    requestDisallowInterceptTouchEvent(false)
                    if (mIsOpenWhenTouchDown && mCloseOnClick) {
                        val confirmView = findConfirmView()
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!(confirmView != null && confirmView.visibility == VISIBLE)) {
                                close()
                            }
                        }, 10)
                    }
                }
                else -> {
                }
            }
            if (mScrollable == -1) {
                mScrollable = calculateScrollableDistance()
            }
            super.dispatchTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isOpen) {
            //不拦截,将事件传递给子View
            mIsOpenWhenTouchDown = true
            return false
        }
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mXDown = ev.x
                mYDown = ev.y
                mXLastMove = mXDown
                mYLastMove = mYDown
            }
            MotionEvent.ACTION_MOVE -> {
                mXMove = ev.x
                mYMove = ev.y
                mXLastMove = mXMove
                mYLastMove = mYMove
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                mXMove = 0f
                mYMove = 0f
                mXLastMove = 0f
                mYLastMove = 0f
            }
            else -> {
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mWrappedView == null) {
            mWrappedView = findContentView()
            if (mWrappedView == null) {
                return false
            }
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mXMove = event.x
                mYMove = event.y
                //计算移动的距离
                val scrolledX = Math.round(mXMove - mXLastMove)
                val scrolledY = Math.round(mYMove - mYLastMove)
                if (Math.abs(scrolledY) > Math.abs(scrolledX) && Math.abs(scrolledY) > CLICK_DISTANCE) {
                    doAnimation()
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    handleDynamicMoveEvent(scrolledX)
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                mXLastMove = mXMove
                mYLastMove = mYMove
                return true
            }
            MotionEvent.ACTION_UP -> {
                mUpTime = System.currentTimeMillis()
                if (mUpTime - mDownTime <= CLICK_INTERVAL &&
                    (Math.abs(event.x - mX) < CLICK_DISTANCE && Math.abs(event.y - mY)
                            < CLICK_DISTANCE)
                ) {
                    if (scrollX > CLICK_DISTANCE) {
                        strongClose()
                    } else {
                        performClick()
                    }
                } else {
                    doAnimation()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                strongClose()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (mScroller == null) {
            return
        }
        if (mScroller!!.computeScrollOffset()) {
            scrollTo(mScroller!!.currX, 0)
            invalidate()
        } else {
            callback()
        }
    }

    //是否是展开状态
    val isOpen: Boolean
        get() {
            doNotPerformClick = xOffset >= mScrollable
            return xOffset >= mScrollable
        }

    //如果是展开的，关闭掉
    fun close() {
        if (isOpen) {
            mNotifyCompleteClose = true
            val dx = -mScrollable
            dynamicAnimation(dx)
        }
        val confirmView = findConfirmView()
        if (confirmView != null) {
            confirmView.visibility = INVISIBLE
        }
    }

    fun strongClose() {
        mNotifyCompleteClose = true
        if (mMode == MODE_DYNAMIC) {
            val dx = -mScrollable
            dynamicAnimation(dx)
        } else {
            staticAnimation(mWrappedView!!.translationX, 0.0f)
        }
        val confirmView = findConfirmView()
        if (confirmView != null) {
            confirmView.visibility = INVISIBLE
        }
    }

    //查找被包装的view
    private fun findContentView(): View? {
        val children = childCount
        for (i in 0 until children) {
            val child = getChildAt(i)
            if (child != null) {
                val tag = child.tag
                if (tag == null || tag != null && (tag !is String
                            || ("ctrl" != tag
                            && "confirm" != tag))
                ) {
                    return child
                }
            }
        }
        return null
    }

    private fun findConfirmView(): View? {
        val children = childCount
        for (i in 0 until children) {
            val child = getChildAt(i)
            if (child != null) {
                val tag = child.tag
                if (tag != null && "confirm" == tag) {
                    return child
                }
            }
        }
        return null
    }

    private var deleteDes = "删除"
    fun setDeleteDes(deleteDes: String) {
        this.deleteDes = deleteDes
    }

    //查找ctrl view
    fun findControlView(): List<View> {
        val ctrlView: MutableList<View> = ArrayList()
        val children = childCount
        for (i in 0 until children) {
            val child = getChildAt(i)
            if (child != null) {
                val tag = child.tag
                if (tag != null && "ctrl" == tag) {
                    ctrlView.add(child)
                    child.setOnClickListener { v: View ->
                        if (slideControlViewClickListener != null) {
                            if (!isDeleteConfirm) {
                                slideControlViewClickListener!!.onSlideControlViewClickListener(i)
                                close()
                            } else {
                                var isDelete = false
                                isDelete = try {
                                    val isDeleteTextView = v as TextView
                                    isDeleteTextView.text.toString() == deleteDes
                                } catch (e: Exception) {
                                    false
                                }
                                if (isDelete) {
                                    val confirmView = findConfirmView()
                                    if (confirmView != null) {
                                        confirmView.visibility = VISIBLE
                                        confirmView.setOnClickListener(
                                            OnClickListener { v1: View? ->
                                                close()
                                                slideControlViewClickListener!!.onSlideControlViewClickListener(
                                                    i
                                                )
                                            }
                                        )
                                    }
                                } else {
                                    slideControlViewClickListener!!.onSlideControlViewClickListener(
                                        i
                                    )
                                    close()
                                }
                            }
                        }
                    }
                }
            }
        }
        return ctrlView
    }

    //计算可滚动的距离
    private fun calculateScrollableDistance(): Int {
        //1.优先检查用户动态设定的可滚动值
        var dis = 0
        if (mIsFromFactory) {
            val width = mFactory.widthDp
            if (width != null && width.size > 0) {
                for (w in width) {
                    dis += dip2px(context, w.toFloat())
                }
                return dis
            }
        }
        //2.用户没有指定，则自动计算
        if (mControlView != null) {
            for (v in mControlView!!) {
                dis += v.measuredWidth
            }
        }
        return dis
    }

    private fun init(attr: AttributeSet?) {
        if (attr != null) {
            val a = context.obtainStyledAttributes(attr, R.styleable.SlideWrapper)
            mMode = a.getString(R.styleable.SlideWrapper_mode)
            mCloseOnClick = a.getBoolean(R.styleable.SlideWrapper_close_onclick, true)
            mAnimationDurationMs =
                a.getInt(R.styleable.SlideWrapper_anim_duration, ANIMATION_DURATION)
            if (mMode == MODE_DYNAMIC) {
                mScroller = Scroller(context)
            }
            a.recycle()
        }
        mCallback = CallbackWrapper(null)
    }

    private fun scroll(xoffset: Int) {
        if (mMode == MODE_STATIC) {
            //静态的时候滚动view自己
            if (mWrappedView != null) {
                mWrappedView!!.translationX = xoffset.toFloat()
            }
        } else {
            // 动态的时候滚动内容
            // scrollBy 相对于当前View的位置进行滑动
            // scrollTo 相对于绝对坐标进行滑动（相对于屏幕坐标进行滑动）
            // event.getRawX  触摸点距离屏幕左边界的距离(MotionEvent中的方法，与触摸点相关)
            // event.getX     触摸点距离响应触摸事件的View的左边界的距离(MotionEvent中的方法，与触摸点相关)
            // view.getLeft() view左边界相对其父View左边界的距离
            // view.getTranslationX() 该View在X轴的偏移量。向左偏移为负值,向右偏移为正值
            // getScrollX() 当前View左边界相对于View初始位置X轴上的偏移量(View的左边界在屏幕的左侧，为负值。 相反为正值)
            // 可以认为是原点（0,0）x轴坐标减去移动后的View视图左上角x轴坐标的值。
            // xoffset  为滑动的距离，实际上并不需要向右滑动如此长的距离 xoffset = mXMove - mXLastMove
            var realXOffset = xoffset
            if (xoffset < 0 && scrollX + xoffset < 0) {
                realXOffset = -scrollX
            }
            scrollBy(realXOffset, 0)
        }
    }

    //复位，滚回原处
    private fun reset() {
        scrollTo(0, 0)
    }

    private val xOffset: Int
        private get() {
            val scrollX = if (mMode == MODE_DYNAMIC) scrollX else mWrappedView!!.translationX
                .toInt()
            return Math.abs(scrollX)
        }

    //做动画
    private fun doAnimation() {
        val scrollX = scrollX
        var dx = 0
        var close = true
        val viewConfiguration = ViewConfiguration.get(BaseApplication.getContext())
        val touchSlop = viewConfiguration.scaledTouchSlop
        val scrolledX = Math.round(mXMove - mXDown)
        if (scrolledX > touchSlop) {
            dx = -scrollX
            mNotifyPullback = if (mNotifyCompleteClose) false else true
        } else {
            if (scrollX < mScrollable / 3) {
                //小于1/3 复原
                dx = -scrollX
                mNotifyPullback = if (mNotifyCompleteClose) false else true
            } else {
                //大于1/3 展开
                mNotifyCompleteOpen = true
                close = false
                dx = mScrollable - scrollX
            }
        }
        if (mMode == MODE_STATIC) {
            //静态模式，用属性动画来做
            staticAnimation(mWrappedView!!.translationX, if (close) 0.0f else -mScrollable.toFloat())
        } else {
            //动态模式，用Scroller来做动画
            dynamicAnimation(dx)
        }
    }

    private fun dynamicAnimation(dx: Int) {
        var realDx = dx
        if (scrollX + dx < 0 && dx < 0) {
            realDx = -scrollX
        }
        mScroller!!.startScroll(scrollX, 0, realDx, 0, mAnimationDurationMs)
        invalidate()
    }

    private fun staticAnimation(from: Float, to: Float) {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = mAnimationDurationMs.toLong()
        animator.addUpdateListener { valueAnimator: ValueAnimator? ->
            val v = animator.animatedValue as Float
            mWrappedView!!.translationX = v
            if (v == to) {
                callback()
            }
        }
        animator.start()
    }

    private fun callback() {
        if (mCallback == null) {
            return
        }
        if (mNotifyCompleteOpen) {
            mCallback!!.onCompletelyOpen(this)
            mNotifyCompleteOpen = false
        }
        if (mNotifyPullback) {
            mCallback!!.onPullBack(this)
            doNotPerformClick = false
            mNotifyPullback = false
        }
        if (mNotifyCompleteClose) {
            mCallback!!.onCompleteClosed(this)
            mNotifyCompleteClose = false
        }
    }

    private fun handleDynamicMoveEvent(scrolledX: Int) {
        // getScrollX() 当前View左边界相对于View初始位置X轴上的偏移量(View的左边界在屏幕的左侧，为负值。 相反为正值)
        // 可以认为是原点（0,0）x轴坐标减去移动后的View视图左上角x轴坐标的值。
        // scrolled 已经偏移的X轴上的距离
        var scrolledX = scrolledX
        val scrolled = Math.abs(scrollX)
        if (scrolledX < 0) {
            //向左滑动
            if (scrolled < mScrollable) {
                if (scrolled + Math.abs(scrolledX) >= mScrollable) {
                    scrolledX = 0
                }
                scroll(-scrolledX)
            }
        } else {
            //向右滑动
            if (scrolled > 0) {
                if (scrolled - scrolledX <= 0) {
                    scrolledX = 0
                    //这里会有一点偏移，手动滚回原位
                    reset()
                }
            }
            scroll(-scrolledX)
        }
    }

    private fun enableControlView(enable: Boolean) {
        if (mEnable != enable) {
            mEnable = enable
            if (mControlView != null && mMode == MODE_STATIC) {
                for (v in mControlView!!) {
                    v.isEnabled = enable
                }
            }
        }
    }

    private val mLongPressRunnable = Runnable { performLongClick() }

    private inner class CallbackWrapper internal constructor(var mRaw: Callback?) : Callback {
        override fun onCompletelyOpen(wrapper: SlideWrapper?) {
            //打开时，如果是静态模式，把侧滑view使能使之可以响应点击事件
            doNotPerformClick = true
            enableControlView(true)
            if (mRaw != null) {
                mRaw!!.onCompletelyOpen(wrapper)
            }
        }

        override fun onCompleteClosed(wrapper: SlideWrapper?) {
            //关闭时，如果是静态模式，把侧滑view禁用不让它响应点击事件
            doNotPerformClick = false
            enableControlView(false)
            if (mRaw != null) {
                mRaw!!.onCompleteClosed(wrapper)
            }
        }

        override fun onPullBack(wrapper: SlideWrapper?) {
            doNotPerformClick = false
            if (mRaw != null) {
                mRaw!!.onPullBack(wrapper)
            }
        }
    }

    private var mXDown = 0f
    private var mYDown = 0f
    private var mXMove = 0f
    private var mXLastMove = 0f
    private var mYMove = 0f
    private var mYLastMove = 0f
    private var mControlView: List<View>? = null
    var mWrappedView: View? = null
    var mScrollable = -1
    private val TAN60 = 1.73f
    private var mFactory: ViewFactory = EmptyViewFactory()
    private var mIsFromFactory = false

    //是否已经确定了阻止父控件拦截触摸事件，提升效率用的标记，避免频繁在dispatchTouchEvent中做浮点运算
    private var mIsDecided = false
    private var mScroller: Scroller? = null
    private var mNotifyCompleteOpen = false
    private var mNotifyCompleteClose = false
    private var mNotifyPullback = false
    private var mCallback: Callback? = null
    private var mAnimationDurationMs = ANIMATION_DURATION
    private var mCloseOnClick = true
    private var mMode: String? = MODE_STATIC
    private var mEnable = true

    //按下时是处于展开状态
    private var mIsOpenWhenTouchDown = false

    //以下用于辅助判断长按和点击事件
    private var mDownTime: Long = 0
    private var mUpTime: Long = 0

    //不触发点击、长按（在展开状态下点击时不触发）
    private var doNotPerformClick = false
    var slideControlViewClickListener: SlideControlViewClickListener? = null
    private var mX = 0f
    private var mY = 0f
    private var mIsMoved = false
    private val CLICK_INTERVAL = 300
    private val CLICK_DISTANCE = 50
    private val LONG_CLICK_INTERVAL = ViewConfiguration.getLongPressTimeout()

    interface SlideControlViewClickListener {
        fun onSlideControlViewClickListener(type: Int)
    }

    fun setmSlideControlViewClickListener(slideControlViewClickListener: SlideControlViewClickListener?) {
        this.slideControlViewClickListener = slideControlViewClickListener
    }

    var isDeleteConfirm = false
    fun setStartDeleteConfirm(startConfirm: Boolean) {
        isDeleteConfirm = startConfirm
    }

    private var enableSlide = true
    fun setEnableSlide(enableSlide: Boolean) {
        this.enableSlide = enableSlide
    }

    companion object {
        private const val TAG = "SlideWrapper"

        /**
         * 静态模式，侧滑view不动，仅被装的view移动
         */
        const val MODE_STATIC = "static"

        /**
         * 动态模式，侧滑view跟随被包装的view一起移动
         */
        const val MODE_DYNAMIC = "dynamic"
        private fun dip2px(context: Context, dipValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }

        private const val ANIMATION_DURATION = 300
    }
}