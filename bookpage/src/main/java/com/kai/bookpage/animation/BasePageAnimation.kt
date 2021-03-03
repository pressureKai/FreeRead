package com.kai.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 *
 * @ProjectName:    My Application
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/19 11:58
 */
abstract class BasePageAnimation : PageAnimation {
    private val TAG = "BasePageAnimation"

    protected var mCurrentBitmap: Bitmap? = null
    protected var mNextBitmap: Bitmap? = null
    protected var isCancel: Boolean = false

    private var mMoveX = 0
    private var mMoveY = 0

    private var isMove = false

    private var isNext = false
    private var noNext = false

    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) :
            super(screenWidth, screenHeight,
                    marginWidth, marginHeight,
                    view,
                    onPageChangeListener) {
    }


    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) :
            super(screenWidth, screenHeight,
                    0, 0,
                    view,
                    onPageChangeListener) {
    }


    fun changePage() {
        val bitmap = mCurrentBitmap
        mCurrentBitmap = mNextBitmap
        mNextBitmap = bitmap
    }


    abstract fun drawStatic(canvas: Canvas)
    abstract fun drawMove(canvas: Canvas)


    override fun onTouchEvent(event: MotionEvent): Boolean {
        //点击位置
        val x = event.x
        val y = event.y

        setTouchPoint(x, y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mMoveX = 0
                mMoveY = 0
                isMove = false
                noNext = false
                isNext = false
                isRunning = false
                isCancel = false
                setStartPoint(x, y)
                abortAnimation()
            }
            MotionEvent.ACTION_MOVE -> {
                val scaledTouchSlop = ViewConfiguration.get(mView?.context).scaledTouchSlop
                if (!isMove) {
                    //判断是否处于滑动状态
                    isMove = abs(mStartX - x) > scaledTouchSlop || abs(mStartY - y) > scaledTouchSlop
                }


                if (isMove) {
                    if (mMoveX == 0 && mMoveY == 0) {
                        //即将开始滑动
                        if (x - mStartX > 0) {
                            //向前翻页
                            isNext = false
                            val hasPrePage = onPageChangeListener.hasPrePage()
                            setDirection(Direction.PRE)
                            if (!hasPrePage) {
                                noNext = true
                                return true
                            }
                        }else{
                            //向下翻页
                            isNext = true
                            val hasNext = onPageChangeListener.hasNext()
                            setDirection(Direction.NEXT)

                            if(!hasNext){
                                noNext = true
                                return true
                            }
                        }
                    } else {
                        //手指在屏幕上来回的滑动
                        isCancel = if(isNext){
                            x - mMoveX > 0
                        }else{
                            x - mMoveX < 0
                        }
                    }

                    mMoveX = x.toInt()
                    mMoveY = y.toInt()
                    isRunning = true
                    mView?.invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if(!isMove){
                    isNext = x >= mScreenWidth / 2
                    if(isNext){
                        val hasNext = onPageChangeListener.hasNext()
                        setDirection(Direction.NEXT)
                        if(!hasNext){
                            return true
                        }
                    }else{
                        val hasPrePage = onPageChangeListener.hasPrePage()
                        setDirection(Direction.PRE)
                        if(!hasPrePage){
                            return true
                        }
                    }
                }

                if(isCancel){
                    onPageChangeListener.pageCancel()
                }


                if(!noNext){
                    startAnimation()
                    mView?.invalidate()
                }
            }
        }

        return true
    }

    override fun draw(canvas: Canvas) {
        if(isRunning){
            drawMove(canvas)
        }else{
            if(isCancel){
                mNextBitmap = mCurrentBitmap?.copy(Bitmap.Config.RGB_565, true)
            }
            drawStatic(canvas)
        }
    }

    override fun scrollAnimation() {
        if(mScroller.computeScrollOffset()){
            val x = mScroller.currX
            val y = mScroller.currY

            setTouchPoint(x.toFloat(),y.toFloat())

            if(mScroller.finalX == x && mScroller.finalY == y){
                isRunning = false
            }
            mView?.postInvalidate()
        }
    }

    override fun abortAnimation() {
        if(!mScroller.isFinished){
            mScroller.abortAnimation()
            isRunning = false
            setTouchPoint(mScroller.finalX.toFloat(),mScroller.finalY.toFloat())
            mView?.postInvalidate()
        }
    }

    override fun getBgBitmap(): Bitmap {
        return mNextBitmap!!
    }

    override fun getNextBitmap(): Bitmap {
        return mCurrentBitmap!!
    }
}