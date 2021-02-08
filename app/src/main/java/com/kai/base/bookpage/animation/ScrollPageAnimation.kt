package com.kai.base.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.kai.base.utils.LogUtils
import java.lang.Exception

/**
 *
 * @ProjectName:    My Application
 * @Description:    翻页动画-上下滑动
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/27 10:09
 */
class ScrollPageAnimation : PageAnimation {

    private val TAG = "ScrollPageAnimation"

    //速度追踪器所设置的时间间隔
    private val velocityTime = 1000

    //速度追踪器
    private var mVelocityTracker: VelocityTracker ?= null


    private val capacity = 2
    private var mBgBitmap: Bitmap? = null
    private var mNextBitmap: Bitmap? = null

    //被废弃的图片列表
    private var mScrapViews: ArrayDeque<BitmapView>? = null


    //正在使用的图片列表
    private var mActiveViews: ArrayList<BitmapView> = ArrayList(capacity)


    private var isRefresh = false


    private var downIt :MutableIterator<BitmapView> ?= null
    private var upIt :MutableIterator<BitmapView> ?= null


    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            marginWidth, marginHeight,
            view, onPageChangeListener) {
        init()
    }

    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) : super(
            screenWidth, screenHeight,
            0, 0,
            view, onPageChangeListener) {
        init()
    }

    private fun init() {
        mBgBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565)

        mScrapViews = ArrayDeque(capacity)

        for (i in 0.until(2)) {
            LogUtils.e(TAG, "I is $i")
            val bitmapView = BitmapView()
            bitmapView.bitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565)
            bitmapView.srcRect = Rect(0, 0, mViewWidth, mViewHeight)
            bitmapView.destRect = Rect(0, 0, mViewWidth, mViewHeight)
            bitmapView.top = 0

            bitmapView.bitmap?.let {
                bitmapView.bottom = it.height
            }

            mScrapViews?.let {
                it.addFirst(bitmapView)
            }

        }

        onLayout()
        isRefresh = false

    }

    private fun onLayout() {
        // 如果还没有加载，则从上到下进行绘制
        if (mActiveViews.size == 0) {
            fillDown(0, 0)
            mDirection = Direction.NONE
        } else {
            val offset = (mTouchX - mStartX).toInt()
            if (offset > 0) {
                val topEdge = mActiveViews[0].top
                fillUp(topEdge, offset)
            } else {
                val bottomEdge = mActiveViews[mActiveViews.size - 1].bottom
                fillDown(bottomEdge, offset)
            }
        }

    }


    /**
     * 创建View填充顶部空白部分
     *@param topEdge 当前第一个View的顶部，到屏幕顶部的距离
     *@param offset 滑动的偏移量
     */
    private fun fillUp(topEdge: Int, offset: Int) {
        // 首先进行布局的调整
        upIt = mActiveViews.iterator()
        var activeView :BitmapView ?= null
        upIt?.let {
            while (it.hasNext()){
                activeView = it.next()
                activeView?.let { view ->
                    view.top = view.top + offset
                    view.bottom = view.bottom + offset
                    // 设置允许显示的范围
                    view.destRect?.top = view.top
                    view.destRect?.bottom = view.bottom


                    //判断是否越界

                    if(view.top >= mViewHeight){
                        // 添加到废弃的View中
                        mScrapViews?.add(view)
                        // 从Active中移除
                        upIt?.remove()


                        // 如果原先是下，现在变成从上加载了，则表示取消加载


                        if(mDirection == Direction.DOWN){

                            onPageChangeListener.pageCancel()
                            mDirection = Direction.NONE

                        }
                    }
                }
            }
        }
        // 活动之后，第一个 View 的顶部 距离 屏幕顶部的实际位置
        var realEdge = topEdge + offset


        while (realEdge > 0 && mActiveViews.size < 2){
            // 从废弃的Views 中获取第一个
             mScrapViews?.let{
                activeView = it.first()
             }

            if(activeView == null){
                return
            }


            activeView?.let { view ->
                val cancelBitmap = mNextBitmap
                mNextBitmap = view.bitmap
                if(!isRefresh){
                    val hasPrePage = onPageChangeListener.hasPrePage()
                    // 如果不存在next，则进行还原
                    if(!hasPrePage){

                        mNextBitmap = cancelBitmap
                        for(activeView in mActiveViews){
                            activeView.top = 0
                            activeView.bottom = mViewHeight
                            // 设置允许显示的范围
                            activeView.destRect?.top = activeView.top
                            activeView.destRect?.bottom = activeView.bottom
                        }
                        abortAnimation()
                        return

                    }
                }


                // 如果加载成功，那么就将View从ScrapViews 中移除

                mScrapViews?.removeFirst()

                // 加入到存活的bitmapView 列表中
                mActiveViews.add(0, view)
                mDirection = Direction.UP


                // 设置Bitmap 的范围

                view.bitmap?.let {
                    view.top = realEdge -  it.height
                }

                view.bottom = realEdge

                //设置允许显示的范围
                view.destRect?.top = view.top
                view.destRect?.bottom = view.bottom

                view.bitmap?.let {
                    realEdge -= it.height
                }

            }

        }



    }

    /**
     * 创建View 填充底部空白部分
     * @param bottomEdge 当前最后一个View的底部，在整个屏幕的位置，即相对屏幕顶部的距离
     * @param offset 滑动的偏移量
     */
    private fun fillDown(bottomEdge: Int, offset: Int) {

        downIt = mActiveViews.iterator()
        var bitmapView :BitmapView ?= null

        downIt?.let { iterator ->

            //删除操作
            while (iterator.hasNext()){
                bitmapView = iterator.next()
                bitmapView?.let {
                    it.top = it.top + offset
                    it.bottom = it.bottom + offset

                    it.destRect?.top = it.top
                    it.destRect?.bottom = it.bottom


                    if(it.bottom < 0){
                        mScrapViews?.add(it)
                        downIt?.remove()

                        //如果原先是从上加载，现在变成从下加载，则表示取消
                        if(mDirection == Direction.UP){
                            onPageChangeListener.pageCancel()
                            mDirection = Direction.NONE
                        }
                    }

                }
            }
        }



        //滑动之后的最后一个View距离屏幕顶部的实际位置
        var realEdge = bottomEdge + offset

        //进行填充

        while(realEdge < mViewHeight && mActiveViews.size < capacity){

            mScrapViews?.let {
                val scrapView = it.first()
                if(scrapView == null){
                    return
                }

                val cancelBitmap = mNextBitmap
                mNextBitmap = scrapView.bitmap

                if(!isRefresh){
                    val hasNext = onPageChangeListener.hasNext()
                    //如果不存在next，则进行还原
                    if(!hasNext){
                        mNextBitmap = cancelBitmap
                        for(value in mActiveViews){
                            value.top  = 0
                            value.bottom = mViewHeight
                            value.destRect?.top = value.top
                            value.destRect?.bottom = value.bottom
                        }
                        abortAnimation()
                        return
                    }

                }

                // 如果加载成功，那么就将View从ScrapViews中移除
                mScrapViews?.removeFirst()
                // 添加到存活的Bitmap中
                mActiveViews.add(scrapView)
                mDirection = Direction.DOWN

                //设置Bitmap 的范围
                scrapView.top = realEdge
                var viewHeight = 0
                scrapView.bitmap?.let {
                    viewHeight = it.height
                }
                scrapView.bottom = realEdge + viewHeight


                // 设置允许显示的范围
                scrapView.destRect?.top = scrapView.top
                scrapView.destRect?.bottom = scrapView.bottom

                scrapView.bitmap?.let {
                    realEdge += it.height
                }

            }

        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x.toInt()
        val y = event.y.toInt()


        // 初始化速度追踪器
        if(mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain()
        }


        mVelocityTracker?.addMovement(event)

        // 设置触碰点
        setTouchPoint(x.toFloat(),y.toFloat())

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                isRunning = false
                setStartPoint(x.toFloat(),y.toFloat())
                abortAnimation()
            }

            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker?.computeCurrentVelocity(velocityTime)
                isRunning = true
                mView?.postInvalidate()
            }
            MotionEvent.ACTION_UP -> {
                isRunning = false
                startAnimation()
                mVelocityTracker?.recycle()
                mVelocityTracker = null

            }
            MotionEvent.ACTION_CANCEL ->{
                try {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }
        return true
    }


    private var tmpView :BitmapView ?= null
    override fun draw(canvas: Canvas) {

        //进行布局
        onLayout()


        //绘制背景
        mBgBitmap?.let {
            canvas.drawBitmap(it,0f,0f,null)
        }


        //绘制内容
        canvas.save()


        //移动位置
        canvas.translate(0f,mMarginHeight.toFloat())

        //裁剪显示区域
        canvas.clipRect(0,0,mViewWidth,mViewHeight)



        //绘制Bitmap
        for(value in mActiveViews){
            tmpView = value
            tmpView?.let {
                try {
                    canvas.drawBitmap(it.bitmap!!,
                            it.srcRect,
                            it.destRect!!,
                            null)
                }catch (e:Exception){

                }

            }

        }


    }

    override fun scrollAnimation() {
        if(mScroller.computeScrollOffset()){
            val x = mScroller.currX
            val y = mScroller.currY
            setTouchPoint(x.toFloat(), y.toFloat())

            if(mScroller.finalX == x
                    && mScroller.finalY == y){
                isRunning = false
            }
            mView?.postInvalidate()
        }

    }

    override fun abortAnimation() {

        if(!mScroller.isFinished){
            mScroller.abortAnimation()
            isRunning = false
        }


    }

    override fun getBgBitmap(): Bitmap {
        return mBgBitmap!!
    }

    override fun getNextBitmap(): Bitmap {
        return mNextBitmap!!
    }


    private class BitmapView {
        var bitmap: Bitmap? = null
        var srcRect: Rect? = null
        var destRect: Rect? = null
        var top = 0
        var bottom = 0
    }


    @Synchronized
    override fun startAnimation() {

        isRunning = true


        var yVelovcity = 0
        mVelocityTracker?.let {
            yVelovcity =  it.yVelocity.toInt()
        }

        mScroller.fling(0,
                mTouchY.toInt(),
                0,
                yVelovcity,
                0,
                0,
                Integer.MAX_VALUE * -1,
                Integer.MAX_VALUE)

    }
}