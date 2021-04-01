package com.kai.common.extension

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.gyf.immersionbar.ImmersionBar
import es.dmoral.toasty.Toasty


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



fun Fragment.customToast(charSequence: String) {
    val normal = Toasty.normal(this.requireContext(), charSequence)
    normal.setGravity(Gravity.CENTER, 0, 0)
    normal.show()
}
