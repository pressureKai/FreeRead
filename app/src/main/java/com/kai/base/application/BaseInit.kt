package com.kai.base.application

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.base.R
import com.kai.common.application.BaseAppInit
import com.kai.common.constant.Constant
import com.kai.common.utils.LogUtils
import com.kai.common.utils.SharedPreferenceUtils
import com.kai.ui.fonts.FontsActivity
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import skin.support.SkinCompatManager
import skin.support.app.SkinAppCompatViewInflater
import skin.support.app.SkinCardViewInflater
import skin.support.constraint.app.SkinConstraintViewInflater
import skin.support.design.app.SkinMaterialViewInflater
import java.util.*


/**
 * com.kai.base 此包的 application 初始化
 */
class BaseInit : BaseAppInit {
    override fun onInitSpeed(application: Application): Boolean {

        return true
    }

    override fun onInitLow(application: Application): Boolean {
        initBugly(application)
        initSkin(application)
        initARouter(application)
        initCalligraphy()
        RxJavaPlugins.setErrorHandler {

        }
        return true
    }

    private fun initBugly(application: Application) {
        try {
            // val channel = WalleChannelReader.getChannel(application)
            val strategy = UserStrategy(application)
            strategy.appChannel = "Channel${Random().nextInt(100000)}"
            strategy.appVersion = "1.0"
            strategy.appPackageName = "com.kai.base"
            strategy.deviceID = Random().nextInt(100000).toString()
            Bugly.setUserId(application, Random().nextInt(100000).toString())
            Beta.autoCheckUpgrade = false
            Bugly.init(application, Constant.buglyId, Constant.isDebug, strategy)
        } catch (e: java.lang.Exception) {
            LogUtils.e("BaseInit", "init bugly error $e")
        }

    }


    private fun initSkin(application: Application) {
        try {
            SkinCompatManager.withoutActivity(application)
                    .addInflater(SkinAppCompatViewInflater()) // 基础控件换肤初始化
                    .addInflater(SkinMaterialViewInflater()) // material design 控件换肤初始化[可选]
                    .addInflater(SkinConstraintViewInflater()) // ConstraintLayout 控件换肤初始化[可选]
                    .addInflater(SkinCardViewInflater()) // CardView v7 控件换肤初始化[可选]
                    .setSkinStatusBarColorEnable(false) // 关闭状态栏换肤，默认打开[可选]
                    .setSkinWindowBackgroundEnable(false) // 关闭windowBackground换肤，默认打开[可选]
                    .loadSkin()
        } catch (e: Exception) {
            LogUtils.e("BaseInit", "init skin error $e")
        }
    }

    private fun initARouter(application: Application) {
        try {
            ARouter.openLog()
            ARouter.openDebug()
            ARouter.init(application)
        } catch (e: java.lang.Exception) {
            LogUtils.e("ARouter", "init error is $e")
        }
    }


    private fun initCalligraphy() {
        var fontPath = "fonts/方正准圆.ttf"
        SharedPreferenceUtils.getInstance()?.let {
            fontPath = it.getString(FontsActivity.CURRENT_FONT)
        }
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath(fontPath)
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())
    }
}