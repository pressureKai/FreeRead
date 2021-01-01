package com.kai.base.application

import android.app.Application
import com.kai.base.utils.LogUtils

class BaseApplication :Application(){
    override fun onCreate() {
        super.onCreate()
        Constant.init(this)
        initModelsSpeed()
        initModelsLow()
    }

    //反射获取BaseInit（子模块中在application 初始化时的操作类统一在BaseApplication执行）
    private fun initModelsSpeed(){
        for(init in PageConfig.initModules){
            try {
                val clazz =  Class.forName(init)
                val moduleInit = clazz.newInstance() as BaseAppInit
                moduleInit.onInitSpeed(this)
            }catch (e :ClassNotFoundException){
                LogUtils.e("BaseApplication","class not found $e")
            }catch (e :IllegalAccessException){
                LogUtils.e("BaseApplication","illegal access $e")
            }catch (e :InstantiationException){
                LogUtils.e("BaseApplication","instantiation $e")
            }
        }
    }

    private fun initModelsLow(){
        for(init in PageConfig.initModules){
            try {
                val clazz =  Class.forName(init)
                val moduleInit = clazz.newInstance() as BaseAppInit
                moduleInit.onInitLow(this)
            }catch (e :ClassNotFoundException){
                LogUtils.e("BaseApplication","class not found $e")
            }catch (e :IllegalAccessException){
                LogUtils.e("BaseApplication","illegal access $e")
            }catch (e :InstantiationException){
                LogUtils.e("BaseApplication","instantiation $e")
            }
        }
    }
}