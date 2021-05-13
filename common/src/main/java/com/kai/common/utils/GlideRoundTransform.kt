package com.kai.common.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.security.MessageDigest

/**
 * created by key  on 2020/5/6
 */
class GlideRoundTransform @JvmOverloads constructor(context: Context?, dp: Int = 4) :
    BitmapTransformation() {
    private var radius = 0f
    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight)
        return roundCrop(pool, bitmap)!!
    }

    private fun roundCrop(pool: BitmapPool, source: Bitmap?): Bitmap? {
        if (source == null) return null
        var result: Bitmap? = pool[source.width, source.height, Bitmap.Config.ARGB_8888]
        if (result == null) {
            result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(result!!)
        val paint = Paint()
        paint.shader =
            BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true
        val rectF = RectF(
            0f, 0f, source.width.toFloat(), source.height
                .toFloat()
        )
        canvas.drawRoundRect(rectF, radius, radius, paint)
        return result
    }

    val id: String
        get() = javaClass.name + Math.round(radius)

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

    init {
        radius = Resources.getSystem().displayMetrics.density * dp
    }
}