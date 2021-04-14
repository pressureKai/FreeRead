package com.kai.bookpage.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.kai.bookpage.animation.*
import com.kai.bookpage.model.CoolBookBean
import com.kai.common.utils.LogUtils
import java.lang.Exception
import kotlin.math.abs

/**
 * # 页面展示控件，无需关心数据来源，因为数据来源可能有多种方法。降低程序耦合度。
 */
class PageView :View{

    private val TAG = "PageView"
    //当前View的宽呃呃
    private var mViewWidth = 0
    //当前View的高
    private var mViewHeight = 0

    private var mStartX = 0
    private var mStartY = 0
    private var isMove = false


    private var mBgColor = -0x313d64
    private var mPageMode = PageMode.NONE

    //是否允许点击
    private var canTouch = true
    //唤醒菜单的区域
    private var mCenterRect : RectF  ?= null
    private var isPrepare  = false
    //动画类
    private var mPageAnimation : PageAnimation?= null
    //动画监听类
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

    //点击监听
    private var mTouchListener : TouchListener ?= null
    //内容加载器
    private var mPageLoader : PageLoader ?= null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) :this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        //监听到onSizeChanged 即代表页面准备完毕
        isPrepare = true
        LogUtils.e("PageView","from on size changed")
        mPageLoader?.prepareDisplay(w, h)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPageAnimation?.abortAnimation()
        mPageAnimation?.clear()
        mPageLoader = null
        mPageAnimation = null
    }

    fun hasPrePage() :Boolean{
        mTouchListener?.prePage()
        var hasPre = false
        mPageLoader?.let {
            hasPre = it.pre()
        }
        return hasPre
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            //绘制背景
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                it.drawColor(mBgColor)
            } else {
                it.drawColor(context.resources.getColor(android.R.color.white))
            }
            //绘制动画
            mPageAnimation?.draw(canvas)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.let {
            if(!canTouch && event.action != MotionEvent.ACTION_DOWN){
                return true
            }


            val x = event.x.toInt()
            val y = event.y.toInt()
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    mStartX = x
                    mStartY = y
                    isMove = false
                    mTouchListener?.let {
                        canTouch = it.onTouch()
                    }
                    mPageAnimation?.onTouchEvent(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    //判断是否大于最小滑动值
                    val slop = ViewConfiguration.get(context).scaledTouchSlop
                    if (!isMove) {
                        isMove = abs(mStartX - event.x) > slop
                                || abs(mStartY - event.y) > slop
                    }

                    if (isMove) {
                        //如果滑动了，则进行翻页
                        mPageAnimation?.onTouchEvent(event)
                    }
                    LogUtils.e("PageView", "MotionEvent.ACTION_MOVE")
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMove) {
                        //设置中间区域范围
                        if (mCenterRect == null) {
                            mCenterRect = RectF(mViewWidth.toFloat() / 5,
                                    mViewHeight.toFloat() / 3,
                                    mViewHeight.toFloat() * 4 / 5,
                                    mViewHeight.toFloat() * 2 / 3)
                        }

                        //是否点击了中间
                        val contains = mCenterRect?.contains(x.toFloat(), y.toFloat())
                        if (contains != null) {
                            if (contains) {
                                mTouchListener?.center()
                                return true
                            }
                        }
                    }
                    mPageAnimation?.onTouchEvent(event)
                }
                else -> {
                    return true
                }
            }
        }
        return true
    }


    fun hasNextPage():Boolean{
        mTouchListener?.nextPage()
        var hasNext = false
        mPageLoader?.let {
            hasNext = it.next()
        }
        return hasNext
    }


    fun pageCancel(){
        mTouchListener?.cancel()
        mPageLoader?.pageCancel()
    }


    override fun computeScroll() {
        //进行滑动
        mPageAnimation?.scrollAnimation()
        super.computeScroll()
    }



    fun drawCurrentPage(isUpdate: Boolean){
        if(!isPrepare){
            return
        }

        if(!isUpdate){
            if(mPageAnimation is ScrollPageAnimation){
                (mPageAnimation as ScrollPageAnimation).resetBitmap()
            }
        }
        getNextBitmap()?.let {
            mPageLoader?.drawPage(it, isUpdate)
        }

    }

    fun setTouchListener(touchListener: TouchListener){
        mTouchListener = touchListener
    }

    //设置翻页模式
    fun setPageMode(pageMode: PageMode){
        mPageMode = pageMode
        if(mViewWidth == 0 || mViewHeight ==0){
            return
        }
        when(mPageMode){
//            PageMode.SIMULATION -> {
//                LogUtils.e("PageView","init SimulationPageAnimation  $mPageMode")
//                mPageAnimation = SimulationPageAnimation(mViewWidth, mViewHeight, this, mPageAnimationListener)
//            }
//            PageMode.COVER -> {
//                mPageAnimation = CoverAnimation(mViewWidth, mViewHeight, this, mPageAnimationListener)
//            }
//            PageMode.SLIDE -> {
//                mPageAnimation = SlideAnimation(mViewWidth, mViewHeight, this, mPageAnimationListener)
//            }
//            PageMode.NONE -> {
//                LogUtils.e("PageView","init NonePageAnimation $mPageMode")
//                mPageAnimation = NonePageAnimation(mViewWidth, mViewHeight, this, mPageAnimationListener)
//            }
//            PageMode.SCROLL -> {
//                mPageAnimation = ScrollPageAnimation(mViewWidth, mViewHeight, this, mPageAnimationListener)
//            }
            else -> {
                LogUtils.e("PageView","init NonePageAnimation  $mPageMode")
                mPageAnimation = NonePageAnimation(mViewWidth, mViewHeight, this, mPageAnimationListener)
            }
        }
    }

    fun setBgColor(bgColor: Int){
        mBgColor = bgColor
    }

    private fun startPageAnimation(direction: PageAnimation.Direction){
        if(mTouchListener == null){
            return
        }
        abortAnimation()
        if(direction == PageAnimation.Direction.NEXT){
            val x = mViewWidth
            val y = mViewHeight
            //初始化动画
            mPageAnimation?.setStartPoint(x.toFloat(), y.toFloat())
            //设置点击点
            mPageAnimation?.setTouchPoint(x.toFloat(), y.toFloat())
            //设置方向
            mPageAnimation?.setDirection(direction)
            if(!hasNextPage()){
                return
            }
        }else{
            val x = 0
            val y = mViewHeight
            //初始化动画
            mPageAnimation?.setStartPoint(x.toFloat(), y.toFloat())
            //设置点击
            mPageAnimation?.setTouchPoint(x.toFloat(), y.toFloat())
            mPageAnimation?.setDirection(direction)
            if(!hasPrePage()){
                return
            }
        }
        mPageAnimation?.startAnimation()
        postInvalidate()
    }

    //是否能够翻页
    fun autoPrePage() :Boolean{
        var auto = false
        mPageAnimation?.let {
            auto = it !is ScrollPageAnimation
            if(auto){
                startPageAnimation(PageAnimation.Direction.PRE)
            }
        }
        return auto
    }

    //是否能够翻页
    fun autoNextPage() :Boolean{
        var auto = false
        mPageAnimation?.let {
            auto = it !is ScrollPageAnimation
            if(auto){
                startPageAnimation(PageAnimation.Direction.NEXT)
            }
        }
        return false
    }


    fun isPrepare() :Boolean{
        return isPrepare
    }


    fun isRunning() : Boolean{
        if(mPageAnimation == null){
            return false
        }
        return mPageAnimation!!.isRunning()
    }


    fun drawNextPage(){
        if(!isPrepare){
            return
        }
        if(mPageAnimation is BaseHorizontalPageAnimation){
            (mPageAnimation as BaseHorizontalPageAnimation).changePage()
        }
        getNextBitmap()?.let {
            mPageLoader?.drawPage(it, false)
        }
    }

    /**
     * #获取页面内容生成的Bitmap
     * @return bitmap
     */
    private fun getNextBitmap(): Bitmap?{
        if(mPageAnimation == null){
            return null
        }
        return  mPageAnimation?.getNextBitmap()
    }



    /**
      * #  外部调用此方法作为程序入口？
      * @param [coolBookBean] 书籍实体类
      * @return PageLoader 页面加载器
      * @date 2021/4/14
      */
    fun getPageLoader(coolBookBean: CoolBookBean): PageLoader?{
        //判断是否存在,存在直接返回
        if(mPageLoader != null){
            return mPageLoader!!
        }
        //根据数据类型 coolBookBean.isLocal，获取具体加载器
        mPageLoader = if(coolBookBean.isLocal){
            LocalPageLoader(PageView@ this, coolBookBean)
        } else {
            NetPageLoader(PageView@ this, coolBookBean)
        }
        if(mViewWidth != 0 || mViewHeight != 0){
            //初始化PageLoader的屏幕大小
            mPageLoader?.prepareDisplay(mViewWidth, mViewHeight)
        }
        return mPageLoader
    }



    fun getBgBitmap(): Bitmap?{
        if(mPageAnimation == null){
            return null
        }
        return mPageAnimation?.getBgBitmap()
    }

    interface TouchListener{
        fun onTouch() :Boolean
        fun center()
        fun prePage()
        fun nextPage()
        fun cancel()
    }

    //如果滑动状态没有停止就取消状态，重新设置Animation的触碰点
    fun abortAnimation(){
        mPageAnimation?.abortAnimation()
    }
}