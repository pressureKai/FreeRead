package com.kai.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.bumptech.glide.Glide
import com.kai.base.R
import com.kai.common.utils.LogUtils

class ClearAbleEditText : AppCompatEditText {
    private var mContext: Context? = null
    private var cleanTextInterface: CleanTextInterface? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun setCleanTextInterface(cleanTextInterface: CleanTextInterface?) {
        this.cleanTextInterface = cleanTextInterface
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init(context: Context) {
        mContext = context
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        setClearIconVisible(hasFocus() && text.isNotEmpty())
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        setClearIconVisible(focused && length() > 0)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val drawable = compoundDrawables[DRAWABLE_RIGHT]
                if (drawable != null && event.x <= width - paddingRight && event.x >= width - paddingRight - drawable.bounds.width()) {
                    setText("")
                    if (cleanTextInterface != null) cleanTextInterface!!.onClean()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setClearIconVisible(visible: Boolean) {
        Thread{
            LogUtils.e("ClearAbleEditText","height is $height")
            val get = Glide.with(this)
                    .asDrawable()
                    .load(R.drawable.icon_clear_edit)
                    .submit(height, height).get()
            post {
                setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[DRAWABLE_LEFT], compoundDrawables[DRAWABLE_TOP],
                        if (visible) get else null, compoundDrawables[DRAWABLE_BOTTOM])
                compoundDrawablePadding = 8
            }
        }.start()
    }



    interface CleanTextInterface {
        fun onClean()
    }

    companion object {
        private const val DRAWABLE_LEFT = 0
        private const val DRAWABLE_TOP = 1
        private const val DRAWABLE_RIGHT = 2
        private const val DRAWABLE_BOTTOM = 3
    }
}