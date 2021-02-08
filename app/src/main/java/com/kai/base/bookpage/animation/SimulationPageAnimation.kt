package com.kai.base.bookpage.animation

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

class SimulationPageAnimation : BasePageAnimation {
    val tag = "SimulationPageAnimation"
    //拖拽点对应的页脚
    private var mCornerX = 1
    private var mCornerY = 1

    private var mPath0 : Path ?= null
    private var mPath1 : Path ?= null

    //贝塞尔曲线起始点
    private var mBezierStart1: PointF = PointF()
    //贝塞尔曲线控制点
    private var mBezierControl1 :PointF = PointF()
    //贝塞尔曲线顶点
    private var mBezierTop1 :PointF = PointF()
    //贝塞尔曲线结束点
    private var mBezierEnd1 :PointF = PointF()



    //第二条贝塞尔曲线
    private var mBezierStart2: PointF = PointF()
    //贝塞尔曲线控制点
    private var mBezierControl2 :PointF = PointF()
    //贝塞尔曲线顶点
    private var mBezierTop2 :PointF = PointF()
    //贝塞尔曲线结束点
    private var mBezierEnd2 :PointF = PointF()



    private var mMiddleX = 0f
    private var mMiddleY = 0f


    private var mDegrees = 0f

    private var mTouchToCornerDistance = 0f

    private var mColorMatrixFilter:ColorMatrixColorFilter ?= null

    private var mMatrix : Matrix ?= null

    private var mMatrixArray = floatArrayOf(
            0f, 0f,
            0f, 0f,
            0f, 0f,
            0f, 0f,
            1.0f
    )

    //是否属于右上左下
    private var mIsRTAndLB = true
    private var mMaxLength = 0f

    //有阴影的GradientDrawable
    private var mBackShadowDrawableLR : GradientDrawable ?= null
    private var mBackShadowDrawableRL : GradientDrawable ?= null

    private var mFolderShadowDrawableLR : GradientDrawable ?= null
    private var mFolderShadowDrawableRL : GradientDrawable ?= null



    private var mFrontShadowDrawableHBT : GradientDrawable ?= null
    private var mFrontShadowDrawableHTB : GradientDrawable ?= null
    private var mFrontShadowDrawableVLR : GradientDrawable ?= null
    private var mFrontShadowDrawableVRL : GradientDrawable ?= null


    private var mPaint :Paint ?= null

    // 适配 android 高版本无法使用 XOR 的问题
    private var mXORPath : Path ?= null

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
    private fun init(){

        mPath0 = Path()
        mPath1 = Path()
        mXORPath = Path()
        mMaxLength = hypot(mScreenWidth.toDouble(),
                mScreenHeight.toDouble()).toFloat()

        mPaint = Paint()
        mPaint?.style = Paint.Style.FILL


        createDrawable()


        //设置颜色数组
        val colorMatrix = ColorMatrix()

        val array = floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f)



        colorMatrix.set(array)
        mColorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
        mMatrix = Matrix()

        //不让x,y为0，否则在点计算时会有问题
        mTouchX = 0.01f
        mTouchY = 0.01f

    }


    /**
     * 创建阴影的GradientDrawable
     */
    private fun createDrawable(){
        val color = intArrayOf(0x333333, 0xb0333333.toInt())
        mFolderShadowDrawableRL = GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                color)


        mFolderShadowDrawableRL?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }



        mFolderShadowDrawableLR = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                color)


        mFolderShadowDrawableLR?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }



        // 背面颜色数组
        val mBackShadowColors = intArrayOf(
                0xff111111.toInt(),
                0x111111.toInt())

        mBackShadowDrawableRL =
                GradientDrawable(
                        GradientDrawable.Orientation.RIGHT_LEFT,
                        mBackShadowColors)
        mBackShadowDrawableRL?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }



        mBackShadowDrawableLR = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                mBackShadowColors
        )

        mBackShadowDrawableLR?.let{
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }



        // 前面颜色数组

        val mFrontShadowColors = intArrayOf(0x80111111.toInt(),
                0x111111.toInt())

        mFrontShadowDrawableVLR = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                mFrontShadowColors)

        mFrontShadowDrawableVLR?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        mFrontShadowDrawableVRL = GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
                mFrontShadowColors)

        mFrontShadowDrawableVRL?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        mFrontShadowDrawableHTB = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                mFrontShadowColors)

        mFrontShadowDrawableHTB?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        mFrontShadowDrawableHBT = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                mFrontShadowColors)

        mFrontShadowDrawableHBT?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


    }
    override fun drawStatic(canvas: Canvas) {
        if(isCancel){
            mCurrentBitmap?.let{
                mNextBitmap = it.copy(Bitmap.Config.RGB_565, true)
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }else{
            mNextBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    override fun drawMove(canvas: Canvas) {
        when(mDirection){
            Direction.NEXT -> {
                calculatePoints()
                drawCurrentPageArea(canvas, mCurrentBitmap, mPath0)
                drawNextPageAreaAndShadow(canvas, mNextBitmap)
                drawCurrentPageShadow(canvas)
                drawCurrentBackArea(canvas, mCurrentBitmap)
            }
            else -> {
                calculatePoints()
                drawCurrentPageArea(canvas, mNextBitmap, mPath0)
                drawNextPageAreaAndShadow(canvas, mCurrentBitmap)
                drawCurrentPageShadow(canvas)
                drawCurrentBackArea(canvas, mNextBitmap)
            }
        }
    }


    private fun calculatePoints(){


    }


    /**
     * 绘制翻起页背面
     */

    private fun drawCurrentPageArea(canvas: Canvas,
                                    bitmap: Bitmap?,
                                    path: Path?){

        val i = (( mBezierStart1.x + mBezierControl1.x) / 2).toInt()
        val f1 = (abs(i - mBezierControl1.x)).toFloat()
        val i1 = ((mBezierStart2.y + mBezierControl2.y) / 2).toInt()
        val f2 = abs(i1 - mBezierControl2.y).toFloat()
        val f3 = min(f1, f2)


        mPath1?.let {
            it.reset()
            it.moveTo(mBezierTop2.x, mBezierTop2.y)
            it.lineTo(mBezierTop1.x, mBezierTop1.y)
            it.lineTo(mBezierEnd1.x, mBezierEnd1.y)
            it.lineTo(mTouchX, mTouchY)
            it.lineTo(mBezierEnd2.x, mBezierEnd2.y)
            it.close()
        }
        var mFolderShadowDrawable : GradientDrawable ?= null
        var left = 0
        var right = 0
        if(mIsRTAndLB){
            left = (mBezierStart1.x - 1).toInt()
            right = (mBezierStart1.x + f3 + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableLR
        }else{
            left = (mBezierStart1.x - f3 - 1).toInt()
            right = (mBezierStart1.x + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableRL
        }

        canvas.save()


        try {
            mPath0?.let {
                canvas.clipPath(it)
            }

            mPath1?.let {
                canvas.clipPath(it, Region.Op.INTERSECT)
            }

        }catch (e: Exception){

        }

        mPaint?.colorFilter = mColorMatrixFilter

        //对Bitmap进行取色
        val color = bitmap?.getPixel(1, 1)
        var red = 0
        var green = 0
        var blue = 0
        //获取对应的三色
        color?.let {
            /**
             *
             * shl(bits) – 左移位 (Java’s <<)
               shr(bits) – 右移位 (Java’s >>)
               ushr(bits) – 无符号右移位 (Java’s >>>)
               and(bits) – 与
               or(bits) – 或
               xor(bits) – 异或
               inv() – 反向
             */
            red = (it.and(0xff0000) shr 16)
            green = (it.and(0x00ff00) shr 8)
            blue = (it.and(0x0000ff))
        }

        val tempColor = Color.argb(200, red, green, blue)
        val distance = hypot(
                (mCornerX - mBezierControl1.x).toDouble(),
                (mBezierControl2.y - mCornerY).toDouble())
        val f8 = (mCornerX - mBezierControl1.x) / distance
        val f9 = (mBezierControl2.y - mCornerY) / distance


        // why
        mMatrixArray[0] = (1 - 2 * f9 * f9).toFloat()
        mMatrixArray[1] = (2 * f8 * f9).toFloat()
        mMatrixArray[3] = mMatrixArray[1]
        mMatrixArray[4] = (1 - 2 * f8 * f8).toFloat()

        mMatrix?.let {
            it.reset()
            it.setValues(mMatrixArray)
            it.preTranslate(-mBezierControl1.x,-mBezierControl1.y)
            it.postTranslate(mBezierControl1.x,mBezierControl1.y)
            if(bitmap != null){
                canvas.drawBitmap(bitmap,it,mPaint)
            }
        }

        canvas.drawColor(tempColor)
        mPaint?.colorFilter = null
        canvas.rotate(mDegrees,mBezierStart1.x,mBezierStart1.y)

        mFolderShadowDrawable?.setBounds(left,
                mBezierStart1.y.toInt(),
                right,
                (mBezierStart1.y + mMaxLength).toInt())
        mFolderShadowDrawable?.draw(canvas)
        canvas.restore()

    }


    private fun drawNextPageAreaAndShadow(canvas: Canvas,
                                          bitmap: Bitmap?){

    }

    private fun drawCurrentPageShadow(canvas: Canvas){

    }

    private fun drawCurrentBackArea(canvas: Canvas,
                                    bitmap: Bitmap?){

    }

    override fun startAnimation() {
        super.startAnimation()
        // dx 水平方向滑动的距离，负值表示向左滑动
        // dy 垂直方向滑动的距离，负值表示想上滚动
        var dx = 0
        var dy = 0

        if(isCancel){

            dx = if(mCornerX > 0  && mDirection == Direction.NEXT){
                (mScreenWidth - mTouchX).toInt()
            }else{
                mTouchX.toInt() * -1
            }

            if(mDirection != Direction.NEXT ){
                dx = (mScreenWidth + mTouchX).toInt() * -1
            }


            dy = if(mCornerY > 0){
                (mScreenHeight - mTouchY).toInt()
            } else {
                //防止mTouchY 最终变为0
                mTouchY.toInt() * -1
            }

        }else{

            dx = if(mCornerX > 0 && mDirection == Direction.NEXT){
                (mScreenWidth + mTouchX).toInt() * -1
            }else{
                // 加上两个mScreenWidth ?
                (mScreenWidth * 2 - mTouchX).toInt()
            }

            dy = if(mCornerY > 0){
                (mScreenHeight - mTouchY).toInt()
            }else{
                (1- mTouchY).toInt()
            }
        }


        mScroller.startScroll(mTouchX.toInt(),
                mTouchY.toInt(),
                dx, dy, 400)

    }

     override fun setDirection(direction: Direction){
         super.setDirection(direction)
         when(direction){
             Direction.PRE -> {
                 //上一页滑动不出现对角
                 if (mStartX > mScreenHeight / 2) {
                     calculateCornerXY(mStartX, mScreenHeight.toFloat())
                 } else {
                     calculateCornerXY(mScreenWidth - mStartX, mScreenHeight.toFloat())
                 }
             }
             Direction.NEXT -> {
                 if (mScreenWidth / 2 > mStartX) {
                     calculateCornerXY(mScreenWidth - mStartX, mStartY)
                 }
             }
         }
     }


    override fun setStartPoint(x: Float, y: Float) {
        super.setStartPoint(x, y)
        calculateCornerXY(x, y)
    }


    override fun setTouchPoint(x: Float, y: Float) {
        super.setTouchPoint(x, y)
        //触摸y中间位置，把y变成屏幕高度
        if((mStartY > mScreenHeight / 3
                        && mStartY < mScreenHeight * 2 / 3 )
                || mDirection == Direction.PRE){
            mTouchY = mScreenHeight.toFloat()
        }

        if(mStartY > mScreenHeight / 3
                && mStartY < mScreenHeight / 2
                && mDirection == Direction.NEXT){
            mTouchY = 1f
        }
    }


    /**
     *  计算拖拽点对应的拖拽脚
     *  @param x
     *  @param y
     */
    private fun calculateCornerXY(x: Float, y: Float){


    }
}