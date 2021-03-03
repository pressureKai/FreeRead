package com.kai.base.application
import android.app.Application
import com.kai.base.application.BaseAppInit
import com.kai.common.utils.LogUtils

/**
 * com.kai.base 此包的 application 初始化
 */
class BaseInit: BaseAppInit {

    override fun onInitSpeed(application: Application): Boolean {
        LogUtils.e("BaseInit","onInitSpeed")
        return false
    }

    override fun onInitLow(application: Application): Boolean {
        LogUtils.e("BaseInit","onInitLow")
        return false
    }
}