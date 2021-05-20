package com.kai.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.kai.base.R


/**
 * Created by 12457 on 2017/9/4.
 */
class SectorView : View {
    companion object {
        const val DEFAULT_HEIGHT = 8
    }

    private var mContext: Context? = null
    private var radiusHeight = DEFAULT_HEIGHT
    private var startColor = R.color.app_background
    private var endColor = R.color.app_font_color
    private var mBackgroundColor = R.color.app_background
    private var bitmap: Bitmap? = null
    private var controlHeight = 0f
    private var minControlHeight = 0f
    private var maxControlHeight = 0f

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawFilter =
            PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val p = Paint()
        p.isAntiAlias = true
        val shader: Shader = LinearGradient(
            (width / 2).toFloat(), 0f, (width / 2).toFloat(), height.toFloat(), intArrayOf(
                mContext!!.resources.getColor(startColor),
                mContext!!.resources.getColor(endColor)
            ), null, Shader.TileMode.CLAMP
        )
        p.shader = shader
        val mPath = Path()
        mPath.moveTo(0f,0f)
        mPath.lineTo(width.toFloat(),0f)
        mPath.lineTo(width.toFloat(),height.toFloat())
        mPath.lineTo(0f,height.toFloat())
        mPath.lineTo(0f,0f)
        canvas.drawPath(mPath,p)
        bitmap?.let {
            canvas.drawBitmap(
                Bitmap.createBitmap(it),
                ((width - it.width) / 2).toFloat(),
                ((height - it.height) / 2).toFloat(),
                null
            )
        }
        if(controlHeight == 0f){
            controlHeight = height.toFloat() / 4 * 5
        }

        if(maxControlHeight == 0f){
            maxControlHeight = height.toFloat() / 4 * 5
        }

        if(minControlHeight == 0f){
            minControlHeight = height/4 * 3.toFloat()
        }
        canvas.save()
        val path = Path()
        path.moveTo(0f, height/4 * 3.toFloat())
        path.quadTo((width/2).toFloat(),controlHeight , width.toFloat(), height/4 * 3.toFloat())
        path.lineTo(width.toFloat(),0f)
        path.lineTo(0f,0f)
        path.lineTo(0f,height/4 * 3.toFloat())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val xor = Path()
            xor.reset()
            xor.moveTo(0f, 0f)
            xor.lineTo(width.toFloat(), 0f)
            xor.lineTo(width.toFloat(), canvas.height.toFloat())
            xor.lineTo(0f, height.toFloat())
            xor.close()
            xor.op(path, Path.Op.XOR)
            canvas.clipPath(xor)
        } else {
            canvas.clipPath(path, Region.Op.XOR)
        }
        canvas.drawColor(mContext!!.resources.getColor(mBackgroundColor))
    }


    fun setChangeHeight(scale: Float) {
        var realScale = scale
        if(scale > 1f){
            realScale = 1f
        }

        if(scale < 0f){
            realScale = 0f
        }
        controlHeight = (maxControlHeight - minControlHeight) * realScale + minControlHeight
        postInvalidate()
    }


    fun setStartAndEndColor(startColor: Int, endColor: Int) {
        this.startColor = startColor
        this.endColor = endColor
        postInvalidate()
    }


    fun getCurrentScale():Float{
       return (controlHeight - minControlHeight).toFloat() / (maxControlHeight - minControlHeight).toFloat()
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        postInvalidate()
    }
    fun setBitmap(bitmap: Drawable) {
      //  this.bitmap = bitmap
        postInvalidate()
    }
}