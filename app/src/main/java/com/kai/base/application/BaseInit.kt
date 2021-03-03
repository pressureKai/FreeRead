package com.kai.base.application
import android.app.Application
import com.kai.common.application.BaseAppInit

/**
 * com.kai.base 此包的 application 初始化
 */
class BaseInit: BaseAppInit {
    override fun onInitSpeed(application: Application): Boolean {
        return false
    }

    override fun onInitLow(application: Application): Boolean {
        return false
    }
}