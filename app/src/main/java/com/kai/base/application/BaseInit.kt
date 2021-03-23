package com.kai.base.application
import android.app.Application
import com.kai.common.application.BaseAppInit
import com.kai.common.constant.Constant
import com.kai.common.utils.LogUtils
import com.tencent.bugly.Bugly
import com.tencent.bugly.crashreport.CrashReport.UserStrategy


/**
 * com.kai.base 此包的 application 初始化
 */
class BaseInit: BaseAppInit {
    override fun onInitSpeed(application: Application): Boolean {
        return true
    }

    override fun onInitLow(application: Application): Boolean {
        initBugly(application)
        return true
    }
    private fun initBugly(application: Application){
        try {
            // val channel = WalleChannelReader.getChannel(application)
            val strategy = UserStrategy(application)
            strategy.appChannel = "myChannel"
            strategy.appVersion = "1.0.5"
            strategy.appPackageName = "com.kai.base"
            strategy.deviceID = "20001"
            Bugly.setUserId(application,"20002")
            Bugly.init(application, Constant.buglyId, Constant.isDebug,strategy)
        }catch (e: java.lang.Exception){
            LogUtils.e("BaseApplication", "init Bugly error $e")
        }

    }
}