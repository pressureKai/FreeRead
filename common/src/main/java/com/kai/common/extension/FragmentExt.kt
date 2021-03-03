package com.kai.common.extension

import android.view.View
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.gyf.immersionbar.ImmersionBar


fun Fragment.initImmersionBar(view: View, fitSystem: Boolean = false, @ColorInt color: Int? = 0) {
    var immersionColor = 0
    color?.let {
        if (it == 0) {
            immersionColor = android.R.color.white
        }
    }
    ImmersionBar
        .with(this)
        .statusBarDarkFont(true, 0.7f)
        .fitsSystemWindows(fitSystem)
        .titleBar(view)
        .navigationBarColor(immersionColor)
        .autoNavigationBarDarkModeEnable(true, 0.8f)
        .init()
}
