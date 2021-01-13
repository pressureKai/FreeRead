package com.kai.base.extension

import android.app.Activity
import android.util.DisplayMetrics
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.kai.base.R


fun Activity.initImmersionBar(view: View, fitSystem: Boolean = false){
    ImmersionBar
            .with(this)
            .statusBarDarkFont(true, 0.7f)
            .fitsSystemWindows(fitSystem)
            .titleBar(view)
            .navigationBarColor(R.color.white)
            .autoNavigationBarDarkModeEnable(true, 0.8f)
            .init()
}

fun Activity.getScreenWidth() :Int{
    val outMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun Activity.getScreenHeight():Int{
    val dm = DisplayMetrics()
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
        windowManager.defaultDisplay.getRealMetrics(dm)
        dm.heightPixels
    }else{
        resources.displayMetrics.heightPixels
    }
}
