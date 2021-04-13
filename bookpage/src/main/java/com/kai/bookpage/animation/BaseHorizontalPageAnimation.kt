package com.kai.bookpage.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 *
 * @ProjectName:    bookPage
 * @Description:    BasePageAnimation 横向动画的模板
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/19 11:58
 */
abstract class BaseHorizontalPageAnimation : PageAnimation {
    private val TAG = "BasePageAnimation"

    protected var mCurrentBitmap: Bitmap? = null
    protected var mNextBitmap: Bitmap? = null
    //是否取消翻页
    protected var isCancel: Boolean = false

    //滑动动作之后最后一个落点的坐标
    private var mMoveX = 0
    private var mMoveY = 0

    //监听触摸事件判断是否处于滑动状态
    private var isMove = false
    //是否翻阅到下一页，true表示下一页，false表示上一页
    private var isNext = false
    //是否没上一页获或者下一页
    private var noNext = false

    /**
     * des  构造方法
     */
    constructor(screenWidth: Int, screenHeight: Int,
                marginWidth: Int, marginHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) :
            super(screenWidth, screenHeight,
                    marginWidth, marginHeight,
                    view,
                    onPageChangeListener)


    /**
     * des 构造方法
     */
    constructor(screenWidth: Int, screenHeight: Int,
                view: View,
                onPageChangeListener: OnPageChangeListener) :
            super(screenWidth, screenHeight,
                    0, 0,
                    view,
                    onPageChangeListener)


    /**
     * des 翻页（前后翻页）
     */
    fun changePage() {
        val bitmap = mCurrentBitmap
        mCurrentBitmap = mNextBitmap
        mNextBitmap = bitmap
    }

    /**
     * des 绘制静态页面不响应页面滑动
     */
    abstract fun drawStatic(canvas: Canvas)

    /**
     * des 绘制动态页面响应页面滑动
     */
    abstract fun drawMove(canvas: Canvas)


    /**
     * des 监听触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //点击位置
        val x = event.x
        val y = event.y
        //设置触摸位置
        setTouchPoint(x, y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //重新赋值变量,并停止动画
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
                //获取滑动的阈值
                val scaledTouchSlop =
                    ViewConfiguration.get(mView?.context).scaledTouchSlop
                if (!isMove) {
                    //判断是否处于滑动状态(竖直滑动与水平滑动)
                    isMove = abs(mStartX - x) > scaledTouchSlop ||
                            abs(mStartY - y) > scaledTouchSlop
                }


                if (isMove) {
                    if (mMoveX == 0 && mMoveY == 0) {
                        //即将开始滑动
                        if (x - mStartX > 0) {
                            //加载上一页
                            isNext = false
                            val hasPrePage = onPageChangeListener.hasPrePage()
                            setDirection(Direction.PRE)
                            if (!hasPrePage) {
                                noNext = true
                                return true
                            }
                        }else{
                            //加载下一页
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
                    //如果不是滑动事件则判断点击事件，根据点击的位置来判断是向前或向后翻页
                    isNext = x >= mScreenWidth / 2
                    if(isNext){
                        //根据回调方法返回的布尔值来判断是否有下一页
                        val hasNext = onPageChangeListener.hasNext()
                        //设置动画的方向
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

    /**
     * des  绘制动画
     * @param canvas 画板
     */
    override fun draw(canvas: Canvas) {
        if(isRunning){
            drawMove(canvas)
        }else{
            if(isCancel){
                //静态页面中取消状态(将当前页复制给下一页)
                mNextBitmap = mCurrentBitmap?.copy(Bitmap.Config.RGB_565, true)
            }
            //继续绘制
            drawStatic(canvas)
        }
    }

    /**
     * des 滑动动画 此段代码的意义是否是达到缓慢滑动的效果
     * unConfirm
     */
    override fun scrollAnimation() {
        if(mScroller.computeScrollOffset()){
            //computeScrollOffset 如果动画尚未完成则返回true
            val x = mScroller.currX
            val y = mScroller.currY

            setTouchPoint(x.toFloat(),y.toFloat())

            if(mScroller.finalX == x && mScroller.finalY == y){
                isRunning = false
            }
            mView?.postInvalidate()
        }
    }

    /**
     * des 取消滑动
     */
    override fun abortAnimation() {
        if(!mScroller.isFinished){
            mScroller.abortAnimation()
            isRunning = false
            setTouchPoint(mScroller.finalX.toFloat(),mScroller.finalY.toFloat())
            mView?.postInvalidate()
        }
    }

    /**
     *des 获取背景面板 unConfirm
     */
    override fun getBgBitmap(): Bitmap ?{
        return mNextBitmap
    }

    /**
     * des 获取下一页面板 unConfirm
     */
    override fun getNextBitmap(): Bitmap? {
        return mNextBitmap
    }
}