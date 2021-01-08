package com.kai.base.application
import android.app.Application

/**
 *  用于子Module  中application初始化
 */
class BaseInit: BaseAppInit {

    override fun onInitSpeed(application: Application): Boolean {
        return false
    }

    override fun onInitLow(application: Application): Boolean {
        return false
    }
}