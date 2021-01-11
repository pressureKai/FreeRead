package com.kai.base.extension

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import com.gyf.immersionbar.ImmersionBar
import com.kai.base.R


fun Activity.initImmersionBar(view:View,fitSystem: Boolean = false){
    ImmersionBar
            .with(this)
            .statusBarDarkFont(true, 0.7f)
            .fitsSystemWindows(fitSystem)
            .titleBar(view)
            .navigationBarColor(R.color.white)
            .autoNavigationBarDarkModeEnable(true,0.8f)
            .init()
}


fun Fragment.initImmersionBar(view:View,fitSystem:Boolean = false){
    ImmersionBar
            .with(this)
            .statusBarDarkFont(true, 0.7f)
            .fitsSystemWindows(fitSystem)
            .titleBar(view)
            .navigationBarColor(R.color.white)
            .autoNavigationBarDarkModeEnable(true,0.8f)
            .init()
}