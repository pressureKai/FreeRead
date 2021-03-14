package com.kai.common.application

import android.annotation.TargetApi
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.multidex.MultiDex
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.common.constant.Constant
import com.kai.common.utils.LogUtils
import com.meituan.android.walle.WalleChannelReader
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.tinker.entry.DefaultApplicationLike


class BaseApplication(
        application: Application, tinkerFlags: Int,
        tinkerLoadVerifyFlag: Boolean, applicationStartElapsedTime: Long,
        applicationStartMillisTime: Long, tinkerResultIntent: Intent
) : DefaultApplicationLike(
        application,
        tinkerFlags,
        tinkerLoadVerifyFlag,
        applicationStartElapsedTime,
        applicationStartMillisTime,
        tinkerResultIntent
) {

    companion object{
        private var sInstance :Context ?= null
        fun getContext() :Context?{
            return  sInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = application.baseContext
        Constant.init(application)
        initARouter()
        initModelsSpeed()
        initModelsLow()
        initBugly()
    }

    override fun onBaseContextAttached(base: Context?) {
        super.onBaseContextAttached(base)
        MultiDex.install(base)
        initTinker()
    }


    private fun initARouter(){
        ARouter.init(application)
    }

    private fun initTinker(){
        Beta.installTinker(application)
    }


    //反射获取BaseInit（子模块中在application 初始化时的操作类统一在BaseApplication执行）
    private fun initModelsSpeed(){
        for(init in ModuleConfig.initModules){
            try {
                val clazz =  Class.forName(init)
                val moduleInit = clazz.newInstance() as BaseAppInit
                moduleInit.onInitSpeed(application)
            }catch (e: ClassNotFoundException){
                LogUtils.e("BaseApplication", "class not found $e")
            }catch (e: IllegalAccessException){
                LogUtils.e("BaseApplication", "illegal access $e")
            }catch (e: InstantiationException){
                LogUtils.e("BaseApplication", "instantiation $e")
            }
        }
    }

    private fun initModelsLow(){
        for(init in ModuleConfig.initModules){
            try {
                val clazz =  Class.forName(init)
                val moduleInit = clazz.newInstance() as BaseAppInit
                moduleInit.onInitLow(application)
            }catch (e: ClassNotFoundException){
                LogUtils.e("BaseApplication", "class not found $e")
            }catch (e: IllegalAccessException){
                LogUtils.e("BaseApplication", "illegal access $e")
            }catch (e: InstantiationException){
                LogUtils.e("BaseApplication", "instantiation $e")
            }
        }
    }


    private fun initBugly(){
        val channel = WalleChannelReader.getChannel(application)
        Bugly.setAppChannel(application, channel)
        Bugly.init(application, Constant.buglyId, Constant.isDebug)
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun registerActivityLifecycleCallback(callbacks: ActivityLifecycleCallbacks?) {
        application.registerActivityLifecycleCallbacks(callbacks)
    }
}