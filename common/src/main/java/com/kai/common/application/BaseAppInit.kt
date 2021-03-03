package com.kai.common.application

import android.app.Application

/**
 *  子模块在Application 中的初始化方法接口
 */
interface BaseAppInit {
    fun onInitSpeed(application: Application):Boolean

    fun onInitLow(application :Application):Boolean
}