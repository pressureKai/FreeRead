package com.kai.base.application

import android.app.Application
import android.content.pm.ApplicationInfo
import com.kai.base.utils.LogUtils
import java.lang.Exception

class Constant {
    companion object{
        var isDebug = false
        fun init(application : Application){
            isDebug = isDebugAble(application)
        }


        private fun isDebugAble(application: Application) :Boolean{
            try {
                val applicationInfo = application.applicationInfo
                val flags = applicationInfo.flags
                val flagDebuggable = ApplicationInfo.FLAG_DEBUGGABLE
                return (flags and flagDebuggable) != 0
            }catch (e:Exception){
                LogUtils.e("Constant",e.toString())
            }
            return false
        }

    }
}