package com.kai.base.application

import android.annotation.TargetApi
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.multidex.MultiDex
import com.kai.base.utils.LogUtils
import com.tencent.bugly.Bugly
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
    override fun onCreate() {
        super.onCreate()
        Constant.init(application)
        initModelsSpeed()
        initModelsLow()
        initBugly()
    }

    override fun onBaseContextAttached(base: Context?) {
        super.onBaseContextAttached(base)
        MultiDex.install(base)
        initTinker()
    }

    private fun initTinker(){

    }




    //反射获取BaseInit（子模块中在application 初始化时的操作类统一在BaseApplication执行）
    private fun initModelsSpeed(){
        for(init in PageConfig.initModules){
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
        for(init in PageConfig.initModules){
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
        Bugly.init(application, "900029763", Constant.isDebug);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun registerActivityLifecycleCallback(callbacks: ActivityLifecycleCallbacks?) {
        application.registerActivityLifecycleCallbacks(callbacks)
    }
}