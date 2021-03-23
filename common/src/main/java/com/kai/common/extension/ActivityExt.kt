package com.kai.common.extension

import android.app.Activity
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.gyf.immersionbar.ImmersionBar
import kotlin.math.abs


fun Activity.initImmersionBar(
    view: View? = null,
    fitSystem: Boolean = false,
    @ColorInt color: Int? = 0
) {
    var immersionColor = 0
    color?.let {
        if (it == 0) {
            immersionColor = android.R.color.white
        }
    }
    if(view != null){
        ImmersionBar
            .with(this)
            .statusBarDarkFont(true, 0.7f)
            .fitsSystemWindows(fitSystem)
            .titleBar(view)
            .navigationBarColor(immersionColor)
            .autoNavigationBarDarkModeEnable(true, 0.8f)
            .init()
    }else{
        ImmersionBar
            .with(this)
            .statusBarDarkFont(true, 0.7f)
            .fitsSystemWindows(fitSystem)
            .navigationBarColor(immersionColor)
            .autoNavigationBarDarkModeEnable(true, 0.8f)
            .init()
    }
}

fun Activity.getScreenWidth(): Int {
    val outMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun Activity.getScreenHeightReal(): Int {
    val dm = DisplayMetrics()
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
        windowManager.defaultDisplay.getRealMetrics(dm)
        dm.heightPixels
    } else {
        resources.displayMetrics.heightPixels
    }
}

fun Activity.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun Activity.getStatusBarHeight(): Int {
    var statusHeight = -1
    try {
        val clazz = Class.forName("com.android.internal.R\$dimen")
        val `object` = clazz.newInstance()
        val height = clazz.getField("status_bar_height")[`object`].toString().toInt()
        statusHeight = resources.getDimensionPixelSize(height)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return statusHeight
}

fun Activity.getNavigationBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

fun Activity.getRealHeightWithKeyboard(keyboardHeight: Int): Int {
    var realHeight = keyboardHeight
    //屏幕实际高度
    val screenHeightReal = getScreenHeightReal()
    //屏幕可用高度
    val screenHeight = getScreenHeight()
    //底部导航栏高度
    val navigationBarHeight = getNavigationBarHeight()

    //屏幕导航栏高度加上屏幕可用高度小于屏幕实际高度视为正常情况(异常情况为某些机型上ROM添加了底部非系统手势线)
    if (navigationBarHeight + screenHeight < screenHeightReal) {
        if (screenHeight != screenHeightReal) {
            realHeight += abs(screenHeightReal - screenHeight)
            if (isNavigationBarExist()) {
                realHeight -= navigationBarHeight
            }
        }
    }

    return realHeight
}


fun Activity.isNavigationBarExist(): Boolean {
    val vp = window.decorView as ViewGroup
    if (vp != null) {
        for (i in 0 until vp.childCount) {
            vp.getChildAt(i).context.packageName
            if (vp.getChildAt(i).id != View.NO_ID && "navigationBarBackground" == resources
                    .getResourceEntryName(vp.getChildAt(i).id)
            ) {
                return true
            }
        }
    }
    return false
}
