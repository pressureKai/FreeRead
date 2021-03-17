package com.kai.bookpage.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.kai.bookpage.animation.*
import com.kai.bookpage.model.CoolBookBean

class PageView :View{

    private val TAG = "PageView"
    //当前View的宽呃呃
    private var mViewWidth = 0
    //当前View的高
    private var mViewHeight = 0

    private var mStartX = 0
    private var mStartY = 0
    private var isMove = false


    private var mBgColor = 0xFFCEC29C
    private var mPageMode = PageMode.SIMULATION

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
    constructor(context: Context) : this(context,null)
    constructor(context: Context, attributeSet: AttributeSet?) :this(context, attributeSet,0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        //监听到onSizeChanged 即代表页面准备完毕
        isPrepare = true
        mPageLoader?.prepareDisplay(w,h)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPageAnimation?.abortAnimation()
        mPageAnimation?.clear()
        mPageLoader = null
        mPageAnimation = null
    }

    fun hasPrePage() :Boolean{
        return false
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            //绘制背景
            it.drawColor(mBgColor)
            //绘制动画
            mPageAnimation?.draw(canvas)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        return super.onTouchEvent(event)
    }


    fun hasNextPage():Boolean{
        return false
    }


    fun pageCancel(){

    }

    private fun init(){

    }




    fun drawCurrentPage(isUpdate :Boolean){
        if(!isPrepare){
            return
        }

        if(!isUpdate){
            if(mPageAnimation is ScrollPageAnimation){
                (mPageAnimation as ScrollPageAnimation).resetBitmap()
            }
        }
        getNextBitmap()?.let {
            mPageLoader?.drawPage(it,isUpdate)
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
            PageMode.SIMULATION-> {
                mPageAnimation = SimulationPageAnimation(mViewWidth,mViewHeight,this,mPageAnimationListener)
            }
            PageMode.COVER -> {
                mPageAnimation = CoverAnimation(mViewWidth,mViewHeight,this,mPageAnimationListener)
            }
            PageMode.SLIDE -> {
                mPageAnimation = SlideAnimation(mViewWidth,mViewHeight,this,mPageAnimationListener)
            }
            PageMode.NONE -> {
                mPageAnimation = NonePageAnimation(mViewWidth,mViewHeight,this,mPageAnimationListener)
            }
            PageMode.SCROLL -> {
                mPageAnimation = ScrollPageAnimation(mViewWidth,mViewHeight,this,mPageAnimationListener)
            }
            else -> {
                mPageAnimation = SimulationPageAnimation(mViewWidth,mViewHeight,this,mPageAnimationListener)
            }
        }
    }

    fun setBgColor( bgColor :Int){
        mBgColor = bgColor.toLong()
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
            mPageAnimation?.setStartPoint(x.toFloat(),y.toFloat())
            //设置点击点
            mPageAnimation?.setTouchPoint(x.toFloat(),y.toFloat())
            //设置方向
            mPageAnimation?.setDirection(direction)
            if(!hasNextPage()){
                return
            }
        }else{
            val x = 0
            val y = mViewHeight
            //初始化动画
            mPageAnimation?.setStartPoint(x.toFloat(),y.toFloat())
            //设置点击
            mPageAnimation?.setTouchPoint(x.toFloat(),y.toFloat())
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
        if(mPageAnimation is BasePageAnimation){
            (mPageAnimation as BasePageAnimation).changePage()
        }
        getNextBitmap()?.let {
            mPageLoader?.drawPage(it,false)
        }
    }

    private fun getNextBitmap(): Bitmap?{
        if(mPageAnimation == null){
            return null
        }
        return mPageAnimation?.getNextBitmap()
    }


    fun getPageLoader(coolBookBean: CoolBookBean): PageLoader?{
        //判断是否存在
        if(mPageLoader != null){
            return mPageLoader!!
        }
        //根据数据类型，获取具体加载器
        mPageLoader = if(coolBookBean.isLocal){
            LocalPageLoader(PageView@this,coolBookBean)
        } else {
            NetPageLoader(PageView@this,coolBookBean)
        }
        //判断PageView 是否初始化完成
        if(mViewWidth != 0 || mViewHeight != 0){
            //初始化PageLoader的屏幕大小
            mPageLoader?.prepareDisplay(mViewWidth,mViewHeight)
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