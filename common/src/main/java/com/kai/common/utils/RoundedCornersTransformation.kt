package com.kai.common.utils

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class RoundedCornersTransformation @JvmOverloads constructor(
    private val radius: Int,
    margin: Int,
    cornerType: CornerType = CornerType.ALL
) : BitmapTransformation() {
    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val bitmap = pool[width, height, Bitmap.Config.ARGB_8888]
        bitmap.setHasAlpha(true)
        setCanvasBitmapDensity(toTransform, bitmap)
        val canvas = Canvas(bitmap)
        canvas.drawFilter = PaintFlagsDrawFilter(
            0,
            Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG
        )
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        drawRoundRect(canvas, paint, width.toFloat(), height.toFloat())
        return bitmap
    }

    enum class CornerType {
        ALL, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, BOTTOM, LEFT, RIGHT, OTHER_TOP_LEFT, OTHER_TOP_RIGHT, OTHER_BOTTOM_LEFT, OTHER_BOTTOM_RIGHT, DIAGONAL_FROM_TOP_LEFT, DIAGONAL_FROM_TOP_RIGHT
    }

    private val diameter: Int = radius * 2
    private val margin: Int = margin
    private val cornerType: CornerType = cornerType
    private fun drawRoundRect(canvas: Canvas, paint: Paint, width: Float, height: Float) {
        val right = width - margin
        val bottom = height - margin
        when (cornerType) {
            CornerType.ALL -> canvas.drawRoundRect(
                RectF(
                    margin.toFloat(), margin.toFloat(), right, bottom
                ), radius.toFloat(), radius.toFloat(), paint
            )
            CornerType.TOP_LEFT -> drawTopLeftRoundRect(canvas, paint, right, bottom)
            CornerType.TOP_RIGHT -> drawTopRightRoundRect(canvas, paint, right, bottom)
            CornerType.BOTTOM_LEFT -> drawBottomLeftRoundRect(canvas, paint, right, bottom)
            CornerType.BOTTOM_RIGHT -> drawBottomRightRoundRect(canvas, paint, right, bottom)
            CornerType.TOP -> drawTopRoundRect(canvas, paint, right, bottom)
            CornerType.BOTTOM -> drawBottomRoundRect(canvas, paint, right, bottom)
            CornerType.LEFT -> drawLeftRoundRect(canvas, paint, right, bottom)
            CornerType.RIGHT -> drawRightRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_TOP_LEFT -> drawOtherTopLeftRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_TOP_RIGHT -> drawOtherTopRightRoundRect(canvas, paint, right, bottom)
            CornerType.OTHER_BOTTOM_LEFT -> drawOtherBottomLeftRoundRect(
                canvas,
                paint,
                right,
                bottom
            )
            CornerType.OTHER_BOTTOM_RIGHT -> drawOtherBottomRightRoundRect(
                canvas,
                paint,
                right,
                bottom
            )
            CornerType.DIAGONAL_FROM_TOP_LEFT -> drawDiagonalFromTopLeftRoundRect(
                canvas,
                paint,
                right,
                bottom
            )
            CornerType.DIAGONAL_FROM_TOP_RIGHT -> drawDiagonalFromTopRightRoundRect(
                canvas,
                paint,
                right,
                bottom
            )
            else -> canvas.drawRoundRect(
                RectF(margin.toFloat(), margin.toFloat(), right, bottom),
                radius.toFloat(),
                radius.toFloat(),
                paint
            )
        }
    }

    private fun drawTopLeftRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(
                margin.toFloat(),
                margin.toFloat(),
                (margin + diameter).toFloat(),
                (margin + diameter).toFloat()
            ), radius.toFloat(),
            radius.toFloat(), paint
        )
        canvas.drawRect(
            RectF(
                margin.toFloat(),
                (margin + radius).toFloat(),
                (margin + radius).toFloat(),
                bottom
            ), paint
        )
        canvas.drawRect(RectF((margin + radius).toFloat(), margin.toFloat(), right, bottom), paint)
    }

    private fun drawTopRightRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(right - diameter, margin.toFloat(), right, (margin + diameter).toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(RectF(margin.toFloat(), margin.toFloat(), right - radius, bottom), paint)
        canvas.drawRect(RectF(right - radius, (margin + radius).toFloat(), right, bottom), paint)
    }

    private fun drawBottomLeftRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), bottom - diameter, (margin + diameter).toFloat(), bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                margin.toFloat(),
                margin.toFloat(),
                (margin + diameter).toFloat(),
                bottom - radius
            ), paint
        )
        canvas.drawRect(RectF((margin + radius).toFloat(), margin.toFloat(), right, bottom), paint)
    }

    private fun drawBottomRightRoundRect(
        canvas: Canvas,
        paint: Paint,
        right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(right - diameter, bottom - diameter, right, bottom), radius.toFloat(),
            radius.toFloat(), paint
        )
        canvas.drawRect(RectF(margin.toFloat(), margin.toFloat(), right - radius, bottom), paint)
        canvas.drawRect(RectF(right - radius, margin.toFloat(), right, bottom - radius), paint)
    }

    private fun drawTopRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), margin.toFloat(), right, (margin + diameter).toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(RectF(margin.toFloat(), (margin + radius).toFloat(), right, bottom), paint)
    }

    private fun drawBottomRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), bottom - diameter, right, bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(RectF(margin.toFloat(), margin.toFloat(), right, bottom - radius), paint)
    }

    private fun drawLeftRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), margin.toFloat(), (margin + diameter).toFloat(), bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(RectF((margin + radius).toFloat(), margin.toFloat(), right, bottom), paint)
    }

    private fun drawRightRoundRect(canvas: Canvas, paint: Paint, right: Float, bottom: Float) {
        canvas.drawRoundRect(
            RectF(right - diameter, margin.toFloat(), right, bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(RectF(margin.toFloat(), margin.toFloat(), right - radius, bottom), paint)
    }

    private fun drawOtherTopLeftRoundRect(
        canvas: Canvas,
        paint: Paint,
        right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), bottom - diameter, right, bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(right - diameter, margin.toFloat(), right, bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(margin.toFloat(), margin.toFloat(), right - radius, bottom - radius),
            paint
        )
    }

    private fun drawOtherTopRightRoundRect(
        canvas: Canvas,
        paint: Paint,
        right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), margin.toFloat(), (margin + diameter).toFloat(), bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(margin.toFloat(), bottom - diameter, right, bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                (margin + radius).toFloat(),
                margin.toFloat(),
                right,
                bottom - radius
            ), paint
        )
    }

    private fun drawOtherBottomLeftRoundRect(
        canvas: Canvas,
        paint: Paint,
        right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), margin.toFloat(), right, (margin + diameter).toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(right - diameter, margin.toFloat(), right, bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                margin.toFloat(),
                (margin + radius).toFloat(),
                right - radius,
                bottom
            ), paint
        )
    }

    private fun drawOtherBottomRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(margin.toFloat(), margin.toFloat(), right, (margin + diameter).toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(margin.toFloat(), margin.toFloat(), (margin + diameter).toFloat(), bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(
                (margin + radius).toFloat(),
                (margin + radius).toFloat(),
                right,
                bottom
            ), paint
        )
    }

    private fun drawDiagonalFromTopLeftRoundRect(
        canvas: Canvas, paint: Paint, right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(
                margin.toFloat(),
                margin.toFloat(),
                (margin + diameter).toFloat(),
                (margin + diameter).toFloat()
            ), radius.toFloat(),
            radius.toFloat(), paint
        )
        canvas.drawRoundRect(
            RectF(right - diameter, bottom - diameter, right, bottom), radius.toFloat(),
            radius.toFloat(), paint
        )
        canvas.drawRect(
            RectF(
                margin.toFloat(),
                (margin + radius).toFloat(),
                right - diameter,
                bottom
            ), paint
        )
        canvas.drawRect(
            RectF(
                (margin + diameter).toFloat(),
                margin.toFloat(),
                right,
                bottom - radius
            ), paint
        )
    }

    private fun drawDiagonalFromTopRightRoundRect(
        canvas: Canvas, paint: Paint, right: Float,
        bottom: Float
    ) {
        canvas.drawRoundRect(
            RectF(right - diameter, margin.toFloat(), right, (margin + diameter).toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRoundRect(
            RectF(margin.toFloat(), bottom - diameter, (margin + diameter).toFloat(), bottom),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        canvas.drawRect(
            RectF(margin.toFloat(), margin.toFloat(), right - radius, bottom - radius),
            paint
        )
        canvas.drawRect(
            RectF(
                (margin + radius).toFloat(),
                (margin + radius).toFloat(),
                right,
                bottom
            ), paint
        )
    }

    override fun toString(): String {
        return ("RoundedTransformation(radius=" + radius + ", margin=" + margin + ", diameter="
                + diameter + ", cornerType=" + cornerType.name + ")")
    }

    override fun equals(o: Any?): Boolean {
        return o is RoundedCornersTransformation && o.radius == radius && o.diameter == diameter && o.margin == margin && o.cornerType == cornerType
    }

    override fun hashCode(): Int {
        return ID.hashCode() + radius * 10000 + diameter * 1000 + margin * 100 + cornerType.ordinal * 10
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius + diameter + margin + cornerType).toByteArray(CHARSET))
    }

    fun setCanvasBitmapDensity(toTransform: Bitmap, canvasBitmap: Bitmap) {
        canvasBitmap.density = toTransform.density
    }

    companion object {
        private const val VERSION = 1
        private const val ID =
            "jp.wasabeef.glide.transformations.RoundedCornersTransformation." + VERSION
    }

}