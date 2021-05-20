package com.kai.bookpage.animation

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.View
import com.kai.common.utils.LogUtils
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

class SimulationPageAnimation : BaseHorizontalPageAnimation {
    val tag = "SimulationPageAnimation"

    //拖拽点对应的拖拽脚
    private var mCornerX = 1
    private var mCornerY = 1

    private var mPath0: Path? = null
    private var mPath1: Path? = null

    //贝塞尔曲线起始点
    private var mBezierStart1: PointF = PointF()

    //贝塞尔曲线控制点
    private var mBezierControl1: PointF = PointF()

    //贝塞尔曲线顶点
    private var mBezierTop1: PointF = PointF()

    //贝塞尔曲线结束点
    private var mBezierEnd1: PointF = PointF()


    //第二条贝塞尔曲线
    private var mBezierStart2: PointF = PointF()

    //贝塞尔曲线控制点
    private var mBezierControl2: PointF = PointF()

    //贝塞尔曲线顶点
    private var mBezierTop2: PointF = PointF()

    //贝塞尔曲线结束点
    private var mBezierEnd2: PointF = PointF()

    private var mMiddleX = 0f
    private var mMiddleY = 0f

    private var mDegrees = 0f

    //触摸点距离页面右上角或右下角的直线距离
    private var mTouchToCornerDistance = 0f

    private var mColorMatrixFilter: ColorMatrixColorFilter? = null

    private var mMatrix: Matrix? = null

    private val mMatrixArray = floatArrayOf(
        0f, 0f,
        0f, 0f,
        0f, 0f,
        0f, 0f,
        1.0f
    )

    //是否属于右上左下
    private var mIsRTAndLB = true
    private var mMaxLength = 0f

    //翻页时所产生的阴影GradientDrawable
    private var mBackShadowDrawableLR: GradientDrawable? = null
    private var mBackShadowDrawableRL: GradientDrawable? = null

    private var mFolderShadowDrawableLR: GradientDrawable? = null
    private var mFolderShadowDrawableRL: GradientDrawable? = null


    private var mFrontShadowDrawableHBT: GradientDrawable? = null
    private var mFrontShadowDrawableHTB: GradientDrawable? = null
    private var mFrontShadowDrawableVLR: GradientDrawable? = null
    private var mFrontShadowDrawableVRL: GradientDrawable? = null


    private var mPaint: Paint? = null

    // 适配 android 高版本无法使用 XOR 的问题
    private var mXORPath: Path? = null

    /**
     * des 构造方法
     */
    constructor(
        screenWidth: Int, screenHeight: Int,
        marginWidth: Int, marginHeight: Int,
        view: View,
        onPageChangeListener: OnPageChangeListener
    ) : super(
        screenWidth, screenHeight,
        marginWidth, marginHeight,
        view, onPageChangeListener
    ) {
        init()
    }

    constructor(
        screenWidth: Int, screenHeight: Int,
        view: View,
        onPageChangeListener: OnPageChangeListener
    ) : this(
        screenWidth, screenHeight,
        0, 0,
        view, onPageChangeListener
    )

    /**
     * 初始化 画笔，触摸点，创建相关阴影drawable
     */
    private fun init() {
        mPath0 = Path()
        mPath1 = Path()
        mXORPath = Path()
        mMaxLength = Math.hypot(
            mScreenWidth.toDouble(),
            mScreenHeight.toDouble()
        ).toFloat()

        mPaint = Paint()
        mPaint?.style = Paint.Style.FILL

        //绘制阴影相关drawable
        createDrawable()


        //颜色矩阵
        val colorMatrix = ColorMatrix()
        //矩阵数组所需float数组（用于表示颜色）
        val array = floatArrayOf(
            //红色值
            1f, 0f, 0f, 0f, 0f,
            //绿色值
            0f, 1f, 0f, 0f, 0f,
            //蓝色值
            0f, 0f, 1f, 0f, 0f,
            //透明值
            0f, 0f, 0f, 1f, 0f
        )
        colorMatrix.set(array)
        //4*5颜色矩阵滤色器
        mColorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
        //独一无二的矩阵
        mMatrix = Matrix()

        //不让x,y为0，否则在点计算时会有问题
        mTouchX = 0.01f
        mTouchY = 0.01f

    }


    /**
     * des 绘制动态页面 计算相关Path进行绘制
     */
    override fun drawMove(canvas: Canvas) {
        when (mDirection) {
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

    /**
     * des 绘制静态页面
     */
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mCurrentBitmap?.let {
                mNextBitmap = it.copy(Bitmap.Config.RGB_565, true)
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        } else {
            mNextBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    /**
     * des 触摸事件为MotionEvent.ACTION_UP时开始动画(从触摸位置开始的动画)
     */
    override fun startAnimation() {
        super.startAnimation()
        // dx 水平方向滑动的距离，负值表示向左滑动
        // dy 垂直方向滑动的距离，负值表示想上滚动


        //使用startScroll（）方法执行动画接收五个参数的mTouchX，mTouchY，dx，dy，duration
        var dx = 0
        var dy = 0

        if (isCancel) {
            // 取消翻页，从触摸位置做恢复动画
            dx = if (mCornerX > 0 && mDirection == Direction.NEXT) {
                //翻页角的x轴坐标大于零时 && 向下翻页
                (mScreenWidth - mTouchX).toInt()
            } else {
                //mCornerX <= 0 点击事件的MotionEvent.ACTION_DOWN 点击在屏幕的左半部分区域
                //从后向前翻页（有且唯一一种情况进入以下语句 1.mCornerX <= 0）
                mTouchX.toInt() * -1
            }

            //当mDirection != Direction.Next 时重置 dx
            if (mDirection != Direction.NEXT) {
                dx = (mScreenWidth + mTouchX).toInt() * -1
            }


            dy = if (mCornerY > 0) {
                (mScreenHeight - mTouchY).toInt()
            } else {
                //防止mTouchY 最终变为0
                mTouchY.toInt() * -1
            }

        } else {

            dx = if (mCornerX > 0 && mDirection == Direction.NEXT) {
                (mScreenWidth + mTouchX).toInt() * -1
            } else {
                (mScreenWidth * 2 - mTouchX).toInt()
            }

            dy = if (mCornerY > 0) {
                (mScreenHeight - mTouchY).toInt()
            } else {
                (1 - mTouchY).toInt()
            }
        }



        mScroller.startScroll(
            mTouchX.toInt(),
            mTouchY.toInt(),
            dx, dy, 400
        )

    }


    /**
     *des 设置页面滑动方向并计算拖拽角
     */
    override fun setDirection(direction: Direction) {
        super.setDirection(direction)
        when (direction) {
            Direction.PRE -> {
                //上一页滑动不出现对角
                if (mStartX > mScreenWidth / 2) {
                    //初始触摸位置在屏幕的右半边
                    calculateCornerXY(mStartX, mScreenHeight.toFloat())
                } else {
                    //初始触摸位置在屏幕的左半边
                    calculateCornerXY(mScreenWidth - mStartX, mScreenHeight.toFloat())
                }
            }
            Direction.NEXT -> {
                if (mScreenWidth / 2 > mStartX) {
                    //初始触摸位置在屏幕的右半边
                    calculateCornerXY(mScreenWidth - mStartX, mStartY)
                }
                //初始触摸位置在屏幕的左半边（不做响应）
            }
        }
    }


    /**
     * des 设置起始触摸点同时计算触摸角
     */
    override fun setStartPoint(x: Float, y: Float) {
        super.setStartPoint(x, y)
        calculateCornerXY(x, y)
    }

    /**
     * des 继承自父类重写方法改变mTouchY的数值
     */
    override fun setTouchPoint(x: Float, y: Float) {
        super.setTouchPoint(x, y)
        //触摸中间区域，把y变成屏幕高度
        if ((mStartY > mScreenHeight / 3
                    && mStartY < mScreenHeight * 2 / 3)
            || mDirection == Direction.PRE
        ) {
            mTouchY = mScreenHeight.toFloat()
        }

        //触摸点y轴坐标在某个区域内（startY > mScreenHeight / 3 && mStartY < mScreenHeight / 2），统一将mTouchY 重置为1
        if (mStartY > mScreenHeight / 3
            && mStartY < mScreenHeight / 2
            && mDirection == Direction.NEXT
        ) {
            mTouchY = 1f
        }
    }


    /**
     * 创建阴影的GradientDrawable
     */
    private fun createDrawable() {
        val color = intArrayOf(0x333333, 0xb0333333.toInt())
        mFolderShadowDrawableRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            color
        )


        mFolderShadowDrawableRL?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }



        mFolderShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            color
        )


        mFolderShadowDrawableLR?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        // 背面颜色数组
        val mBackShadowColors = intArrayOf(
            0xff111111.toInt(),
            0x111111.toInt()
        )

        mBackShadowDrawableRL =
            GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                mBackShadowColors
            )
        mBackShadowDrawableRL?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }



        mBackShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            mBackShadowColors
        )

        mBackShadowDrawableLR?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        // 前面颜色数组
        val mFrontShadowColors = intArrayOf(
            0x80111111.toInt(),
            0x111111.toInt()
        )

        mFrontShadowDrawableVLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            mFrontShadowColors
        )

        mFrontShadowDrawableVLR?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        mFrontShadowDrawableVRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            mFrontShadowColors
        )

        mFrontShadowDrawableVRL?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        mFrontShadowDrawableHTB = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            mFrontShadowColors
        )

        mFrontShadowDrawableHTB?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }


        mFrontShadowDrawableHBT = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            mFrontShadowColors
        )

        mFrontShadowDrawableHBT?.let {
            it.gradientType = GradientDrawable.LINEAR_GRADIENT
        }
    }


    /**
     * des 绘制当前页面背景区域
     */
    private fun drawCurrentBackArea(
        canvas: Canvas,
        bitmap: Bitmap?
    ) {
        val i = ((mBezierStart1.x + mBezierControl1.x) / 2).toInt()
        val f1 = abs(i - mBezierControl1.x)
        val i1 = ((mBezierStart2.y + mBezierControl2.y) / 2).toInt()
        val f2 = abs(i1 - mBezierControl2.y)
        val f3 = min(f1, f2)
        mPath1!!.reset()
        mPath1!!.moveTo(mBezierTop2.x, mBezierTop2.y)
        mPath1!!.lineTo(mBezierTop1.x, mBezierTop1.y)
        mPath1!!.lineTo(mBezierEnd1.x, mBezierEnd1.y)
        mPath1!!.lineTo(mTouchX, mTouchY)
        mPath1!!.lineTo(mBezierEnd2.x, mBezierEnd2.y)
        mPath1!!.close()
        var mFolderShadowDrawable: GradientDrawable? = null
        var left = 0
        var right = 0
        if (mIsRTAndLB) {
            left = (mBezierStart1.x - 1).toInt()
            right = (mBezierStart1.x + f3 + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableLR
        } else {
            left = (mBezierStart1.x - f3 - 1).toInt()
            right = (mBezierStart1.x + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableRL
        }

        canvas.save()
        try {
            canvas.clipPath(mPath0!!)
            canvas.clipPath(mPath1!!, Region.Op.INTERSECT)
        } catch (e: java.lang.Exception) {

        }


        mPaint?.colorFilter = mColorMatrixFilter
        var color = 0
        bitmap?.let {
            color = it.getPixel(1, 1)
        }
        //获取对应的三色
        val red = color and 0xff0000 shr 16
        val green = color and 0x00ff00 shr 8
        val blue = color and 0x0000ff
        val tempColor = Color.argb(200, red, green, blue)

        var dis = hypot(
            (mCornerX - mBezierControl1.x).toDouble(),
            (mBezierControl2.y - mCornerY).toDouble()
        )

        var f8 = ((mCornerX - mBezierControl1.x).div(dis)).toFloat()
        var f9 =( (mBezierControl2.y - mCornerY).div(dis)).toFloat()






        mMatrixArray[0] = 1 - (2 * f9 * f9)
        mMatrixArray[1] = 2 * f8 * f9
        mMatrixArray[3] = mMatrixArray[1]
        mMatrixArray[4] = 1 - (2 * f8 * f8)

        mMatrix!!.reset()
        mMatrix!!.setValues(mMatrixArray)
        mMatrix!!.preTranslate(-mBezierControl1.x, -mBezierControl1.y)
        mMatrix!!.postTranslate(mBezierControl1.x, mBezierControl1.y)

        canvas.drawBitmap(bitmap!!, mMatrix!!, mPaint)
        //背景叠加
        canvas.drawColor(tempColor)
        mPaint!!.colorFilter = null
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y)
        mFolderShadowDrawable?.setBounds(
            left,
            mBezierStart1.y.toInt(),
            right,
            (mBezierStart1.y + mMaxLength).toInt()
        )
        mFolderShadowDrawable!!.draw(canvas)
        canvas.restore()
    }


    /**
     * des 绘制当前页面阴影
     */
    private fun drawCurrentPageShadow(canvas: Canvas) {
        var degree = 0.toDouble()
        degree = if (mIsRTAndLB) {
            Math.PI / 4 - Math.atan2(
                (mBezierControl1.y - mTouchY).toDouble(),
                mTouchX.toDouble() - mBezierControl1.x
            )
        } else {
            Math.PI / 4 - Math.atan2(
                (mTouchY - mBezierControl1.y).toDouble(),
                mTouchX.toDouble() - mBezierControl1.x
            )
        }

        val d1 = (25 * 1.414 * Math.cos(degree)).toFloat()
        val d2 = (25 * 1.414 * Math.sin(degree)).toFloat()
        val x = (mTouchX + d1).toFloat()
        val y = if (mIsRTAndLB) {
            mTouchY + d2
        } else {
            mTouchY - d2
        }

        mPath1?.let {
            it.reset()
            it.moveTo(x, y)
            it.lineTo(mTouchX, mTouchY)
            it.lineTo(mBezierControl1.x, mBezierControl1.y)
            it.lineTo(mBezierStart1.x, mBezierStart1.y)
            it.close()
        }
        var rotateDegrees = 0.toFloat()
        canvas.save()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mXORPath?.let { xor ->
                    xor.reset()
                    xor.moveTo(0f, 0f)
                    xor.lineTo(canvas.width.toFloat(), 0f)
                    xor.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
                    xor.lineTo(0f, canvas.height.toFloat())
                    xor.close()
                    xor.op(mPath0!!, Path.Op.XOR)
                    canvas.clipPath(mXORPath!!)
                }
            } else {
                canvas.clipPath(mPath0!!, Region.Op.XOR)
            }
            canvas.clipPath(mPath1!!, Region.Op.INTERSECT)

        } catch (e: java.lang.Exception) {

            LogUtils.e(
                "SimulationPageAnimation",
                "drawCurrentPageShadow error is $e"
            )

        }


        var leftx = 0
        var rightx = 0
        var mCurrentPageShadow: GradientDrawable? = null
        if (mIsRTAndLB) {
            leftx = mBezierControl1.x.toInt()
            rightx = (mBezierControl1.x + 25).toInt()
            mCurrentPageShadow = mFrontShadowDrawableVLR
        } else {
            leftx = (mBezierControl1.x - 25).toInt()
            rightx = (mBezierControl1.x + 1).toInt()
            mCurrentPageShadow = mFrontShadowDrawableVRL
        }


        rotateDegrees = Math.toDegrees(
            Math.atan2(
                (mTouchX - mBezierControl1.x).toDouble(),
                (mBezierControl1.y - mTouchY).toDouble()
            )
        ).toFloat()
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y)

        mCurrentPageShadow?.setBounds(
            leftx,
            (mBezierControl1.y - mMaxLength).toInt(),
            rightx,
            mBezierControl1.y.toInt()
        )

        mCurrentPageShadow?.draw(canvas)
        canvas.restore()


        mPath1?.let {
            it.reset()
            it.moveTo(x, y)
            it.lineTo(mTouchX, mTouchY)
            it.lineTo(mBezierControl2.x, mBezierControl2.y)
            it.lineTo(mBezierStart2.x, mBezierStart2.y)
            it.close()
        }
        canvas.save()


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mXORPath?.let {
                    it.reset()
                    it.moveTo(0f, 0f)
                    it.lineTo(canvas.width.toFloat(), 0f)
                    it.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
                    it.lineTo(0f, canvas.height.toFloat())
                    it.close()
                    it.op(mPath0!!, Path.Op.XOR)
                    canvas.clipPath(mXORPath!!)
                }

            } else {
                canvas.clipPath(mPath0!!, Region.Op.XOR)

            }
            canvas.clipPath(mPath1!!, Region.Op.INTERSECT)
        } catch (e: java.lang.Exception) {
            LogUtils.e("SimulationPageAnimation", "error is $e")
        }

        if (mIsRTAndLB) {
            leftx = mBezierControl2.y.toInt()
            rightx = (mBezierControl2.y + 25).toInt()
            mCurrentPageShadow = mFrontShadowDrawableHTB
        } else {
            leftx = (mBezierControl2.y - 25).toInt()
            rightx = (mBezierControl2.y + 1).toInt()
            mCurrentPageShadow = mFrontShadowDrawableHBT
        }
        rotateDegrees = Math.toDegrees(
            Math.atan2(
                (mBezierControl2.y - mTouchY).toDouble(),
                (mBezierControl2.x - mTouchX).toDouble()
            )
        ).toFloat()
        canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y)

        var temp = 0f
        temp = if (mBezierControl2.y < 0) {
            mBezierControl2.y - mScreenHeight
        } else {
            mBezierControl2.y
        }

        val hmg = Math.hypot(mBezierControl2.x.toDouble(), temp.toDouble())


        mCurrentPageShadow?.let {
            if (hmg > mMaxLength) {
                it.setBounds(
                    (mBezierControl2.x - 25 - hmg).toInt(),
                    leftx,
                    (mBezierControl2.x + mMaxLength - hmg).toInt(),
                    rightx
                )
            } else {
                it.setBounds(
                    (mBezierControl2.x - mMaxLength).toInt(),
                    leftx,
                    (mBezierControl2.x).toInt(),
                    rightx
                )
            }
            it.draw(canvas)
        }
        canvas.restore()
    }


    /**
     *# 绘制下一页的的阴影
     */
    private fun drawNextPageAreaAndShadow(
        canvas: Canvas,
        bitmap: Bitmap?
    ) {
        mPath1!!.reset()
        mPath1!!.moveTo(mBezierStart1.x, mBezierStart1.y)
        mPath1!!.lineTo(mBezierTop1.x, mBezierTop1.y)
        mPath1!!.lineTo(mBezierTop2.x, mBezierTop2.y)
        mPath1!!.lineTo(mBezierStart2.x, mBezierStart2.y)
        mPath1!!.lineTo(mCornerX.toFloat(), mCornerY.toFloat())
        mPath1!!.close()

        mDegrees = Math.toDegrees(
            atan2(
                (mBezierControl1.x - mCornerX).toDouble(),
                (mBezierControl2.y - mCornerY).toDouble()
            )
        )
            .toFloat()
        var leftx = 0
        var rightx = 0
        var mBackShadowDrawable: GradientDrawable? = null

        if (mIsRTAndLB) {
            leftx = mBezierStart1.x.toInt()
            rightx = (mBezierStart1.x + mTouchToCornerDistance / 4).toInt()
            mBackShadowDrawable = mBackShadowDrawableLR
        } else {
            leftx = (mBezierStart1.x - mTouchToCornerDistance / 4).toInt()
            rightx = mBezierStart1.x.toInt()
            mBackShadowDrawable = mBackShadowDrawableRL
        }

        canvas.save()
        try {
            canvas.clipPath(mPath0!!)
            canvas.clipPath(mPath1!!, Region.Op.INTERSECT)
        } catch (e: java.lang.Exception) {

        }

        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y)
        mBackShadowDrawable?.setBounds(
            leftx,
            (mBezierStart1.y).toInt(),
            rightx,
            (mMaxLength + mBezierStart1.y).toInt()
        )
        mBackShadowDrawable?.draw(canvas)
        canvas.restore()
    }


    /**
     *des 绘制当前页面（将页面正文显示与阴影绘制分开）
     */
    private fun drawCurrentPageArea(
        canvas: Canvas,
        bitmap: Bitmap?,
        path: Path?
    ) {
        mPath0?.let {
            it.reset()
            it.moveTo(mBezierStart1.x, mBezierStart1.y)
            it.quadTo(
                mBezierControl1.x,
                mBezierControl1.y,
                mBezierEnd1.x,
                mBezierEnd1.y
            )
            it.lineTo(mTouchX, mTouchY)
            it.lineTo(mBezierEnd2.x, mBezierEnd2.y)
            it.quadTo(
                mBezierControl2.x, mBezierControl2.y,
                mBezierStart2.x, mBezierStart2.y
            )
            it.lineTo(mCornerX.toFloat(), mCornerY.toFloat())
            it.close()
        }

        canvas.save()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mXORPath?.let {
                it.reset()
                it.moveTo(0f, 0f)
                it.lineTo(canvas.width.toFloat(), 0f)
                it.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
                it.lineTo(0f, canvas.height.toFloat())
                it.close()
                it.op(path!!, Path.Op.XOR)
                canvas.clipPath(mXORPath!!)
            }
        } else {
            canvas.clipPath(path!!, Region.Op.XOR)
        }
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        try {
            canvas.restore()
        } catch (e: Exception) {

        }
    }

    /**
     * des 计算相应的贝塞尔曲线所需的各个点
     */
    private fun calculatePoints() {
        mMiddleX = (mTouchX + mCornerX) / 2
        mMiddleY = (mTouchY + mCornerY) / 2


        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX)

        mBezierControl1.y = mCornerY.toFloat()
        mBezierControl2.x = mCornerX.toFloat()


        val f4 = mCornerY - mMiddleY

        if (f4 == 0f) {
            mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / 0.1f
        } else {
            mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY)
        }

        mBezierStart1.x = mBezierControl1.x -  (mCornerX - mBezierControl1.x) / 2
        mBezierStart1.y = mCornerY.toFloat()



        if (mTouchX > 0 && mTouchX < mScreenWidth) {
            if (mBezierStart1.x < 0 || mBezierStart1.x > mScreenWidth) {

                if (mBezierStart1.x < 0) {
                    mBezierStart1.x = mScreenWidth - mBezierStart1.x
                }


                val f1 = abs(mCornerX - mTouchX)
                val f2 = mScreenWidth * f1 / mBezierStart1.x
                mTouchX = abs(mCornerX - f2)


                val f3 = abs(mCornerX - mTouchX) * abs(mCornerY - mTouchY) / f1

                mTouchY = abs(mCornerY - f3)


                mMiddleX = (mTouchX + mCornerX) / 2
                mMiddleY = (mTouchY + mCornerY) / 2


                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX)


                mBezierControl1.y = mCornerY.toFloat()

                mBezierControl2.x = mCornerX.toFloat()
                val f5 = mCornerY - mMiddleY

                if (f5 == 0f) {
                    mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / 0.1f
                } else {
                    mBezierControl2.y = mMiddleY -  (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY)
                }


                mBezierStart1.x = mBezierControl1.x -  (mCornerX - mBezierControl1.x) / 2

            }
        }

        mBezierStart2.x = mCornerX.toFloat()
        mBezierStart2.y = mBezierControl2.y -  (mCornerY - mBezierControl2.y) / 2

        mTouchToCornerDistance = Math.hypot(
            (mTouchX - mCornerX).toDouble(),
            (mTouchY - mCornerY).toDouble()
        ).toFloat()
        mBezierEnd1 = getCross(
            PointF(mTouchX, mTouchY),
            mBezierControl1,
            mBezierStart1,
            mBezierStart2
        )
        mBezierEnd2 = getCross(
            PointF(mTouchX, mTouchY),
            mBezierControl2,
            mBezierStart1,
            mBezierStart2
        )



        mBezierTop1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4
        mBezierTop1.y = (mBezierControl1.y * 2 + mBezierStart1.y + mBezierEnd1.y) / 4
        mBezierTop2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4
        mBezierTop2.y = (mBezierControl2.y * 2 + mBezierStart2.y + mBezierEnd2.y) / 4
    }


    /**
     * des 计算P1，P2，P3，P4 的交点
     */
    private fun getCross(P1: PointF, P2: PointF, P3: PointF, P4: PointF): PointF {
        val pointF = PointF()

        val a1 = (P2.y - P1.y) / (P2.x - P1.x)
        val b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x)


        val a2 = (P4.y - P3.y) / (P4.x - P3.x)
        val b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x)

        pointF.x = (b2 - b1) / (a1 - a2)
        pointF.y = a1 * pointF.x + b1
        return pointF
    }


    /**
     *  计算拖拽点对应的拖拽脚
     *  @param x
     *  @param y
     */
    private fun calculateCornerXY(x: Float, y: Float) {
        //mCornerX 在点击屏幕的左半部分区域时 mCornerX 重置为0 否则 统一为mScreenWidth
        mCornerX = if (x <= mScreenWidth / 2) {
            0
        } else {
            mScreenWidth
        }

        //mCornerY 在点击屏幕的上半部分区域时 mCornerY 重置为0 否则 统一为mScreenHeight
        mCornerY = if (y <= mScreenHeight / 2) {
            0
        } else {
            mScreenHeight
        }

        // mIsRTAndLB  判断是否为右上或左下
        mIsRTAndLB = ((mCornerX == 0 && mCornerY == mScreenHeight)
                || (mCornerX == mScreenWidth && mCornerY == 0))
    }
}