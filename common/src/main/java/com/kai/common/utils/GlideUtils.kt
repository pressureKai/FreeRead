package com.kai.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.kai.common.R
import com.kai.common.utils.ScreenUtils.Companion.dpToPx
import jp.wasabeef.glide.transformations.BlurTransformation
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutionException

/**
 * created by key  on 2020/3/3
 */
object GlideUtils {
    fun loadCenter(context: Context, url: String, iv: ImageView) {
        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .skipMemoryCache(false)
            .transform(CenterCrop())
            .placeholder(R.drawable.loadding)
        loadImage(context, url, options, iv)
    }

    fun load(context: Context, url: String, iv: ImageView) {
        val options = RequestOptions()
            .skipMemoryCache(false)
            .transform(GlideRoundTransform(context, dpToPx(2)))
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        //.fitCenter()
        //fitCenter 缩放图片充满ImageView CenterInside大缩小原(图) CenterCrop大裁小扩充满ImageView  Center大裁(中间)小原
        // .error(R.drawable.loadding);
        loadImage(context, url, options, iv)
    }

    fun loadBlur(context: Context, url: String, iv: View) {
        val options = RequestOptions()
            .skipMemoryCache(false)
            .transform(BlurTransformation(25, 20))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        //.fitCenter()
        //fitCenter 缩放图片充满ImageView CenterInside大缩小原(图) CenterCrop大裁小扩充满ImageView  Center大裁(中间)小原
        // .error(R.mipmap.test_2);
        try {
            loadBlurImage(context, url, options, iv)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun loadGif(context: Context?, iv: ImageView?) {
        val options = RequestOptions()
            .skipMemoryCache(false)
            .transform(GlideRoundTransform(context, dpToPx(2)))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        //.fitCenter()
        // fitCenter 缩放图片充满ImageView CenterInside大缩小原(图) CenterCrop大裁小扩充满ImageView  Center大裁(中间)小原
//                .error(R.mipmap.test_2);
        if (!isDestroy(context as Activity?)) {
            Glide.with(context!!)
                .asGif()
                .load(R.drawable.loadding)
                .apply(options).into(iv!!)
        }
    }

    private fun loadImage(context: Context, url: String, options: RequestOptions, view: ImageView) {
        if (!isDestroy(context as Activity)) {
            Glide.with(context)
                .load(url)
                .apply(options)
                .thumbnail(
                    Glide.with(view)
                        .load(R.drawable.loadding)
                        .apply(options)
                ).into(view)
        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun loadBlurImage(context: Context, url: String, options: RequestOptions, view: View) {
        if (!isDestroy(context as Activity)) {
            val into = Glide.with(context)
                .load(url)
                .apply(options)
                .into(1000, 1000)
            val drawable = into.get()
            context.runOnUiThread { view.background = drawable }
        }
    }

    fun loadCornersTop(context: WeakReference<Context>, url: String?, image: ImageView?, radius :Int,resourceWidthAndHeightListener: ResourceWidthAndHeightListener){
        if (image == null) return
        val requestOptions = RequestOptions().centerCrop()
            .format(DecodeFormat.PREFER_RGB_565)
            .priority(Priority.LOW)
            .error(R.drawable.default_loading)
            .placeholder(R.drawable.default_loading)
            .skipMemoryCache(false)
            .dontAnimate()
            .transform(RoundedCornersTransformation(
                radius,
                0,
                RoundedCornersTransformation.CornerType.TOP
            ))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(context.get()!!.applicationContext)
            .asBitmap()
            .load(url)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .apply(requestOptions)
            .into(object :com.bumptech.glide.request.target.SimpleTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    resourceWidthAndHeightListener.resourceWidthAndHeight(resource.width,resource.height)
                    image.setImageBitmap(resource)
                }
            })
    }

    private fun isDestroy(mActivity: Activity?): Boolean {
        return mActivity == null || mActivity.isFinishing || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed
    }


   public interface ResourceWidthAndHeightListener{
        fun resourceWidthAndHeight(width:Int,height:Int)
    }
}