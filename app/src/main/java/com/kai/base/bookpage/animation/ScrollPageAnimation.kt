package com.kai.base.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.kai.base.utils.LogUtils

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
    private lateinit var mVelocityTracker: VelocityTracker


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
                fillDown(bottomEdge,offset)
            }
        }

    }


    /**
     * 创建View填充顶部空白部分
     *@param topEdge 当前第一个View的顶部，到屏幕顶部的距离
     *@param offset 滑动的偏移量
     */
    private fun fillUp(topEdge: Int, offset: Int) {

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
        val realEdge = bottomEdge + offset

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
                    if(!hasNext){
                        mNextBitmap = cancelBitmap
                    }

                }
            }

        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return false
    }

    override fun draw(canvas: Canvas) {

    }

    override fun scrollAnimation() {

    }

    override fun abortAnimation() {

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

}