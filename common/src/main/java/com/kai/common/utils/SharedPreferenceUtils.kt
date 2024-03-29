package com.kai.common.utils

import android.content.Context
import android.content.SharedPreferences
import com.kai.common.application.BaseApplication

class SharedPreferenceUtils {
    val SHARE_NAME = "IReader_pref"

    var sharedReadable :SharedPreferences ?= null
    var sharedWritable :SharedPreferences.Editor ?= null


    private constructor(){
        val context = BaseApplication.getContext()
        if(context != null){
            sharedReadable = context.getSharedPreferences(SHARE_NAME,
                  Context.MODE_MULTI_PROCESS)
            sharedWritable = sharedReadable?.edit()
        }
    }

    companion object{
        var sInstance :SharedPreferenceUtils ?= null
        fun getInstance() :SharedPreferenceUtils?{
            if(sInstance == null){
                synchronized(SharedPreferenceUtils::class.java){
                    if(sInstance == null){
                        sInstance = SharedPreferenceUtils()
                    }
                }
            }
            return sInstance
        }
    }


    fun getString(key :String):String{
        var value = ""
        sharedReadable?.let {
            val string = it.getString(key, "")
            if(string != null && string.isNotEmpty()){
                value = string
            }
        }
        return value
    }


    fun getString(key :String,default: String):String{
        var value = ""
        sharedReadable?.let {
            val string = it.getString(key, "")
            if(string != null && string.isNotEmpty()){
                value = string
            }
        }
        if(value.isEmpty()){
            value = default
        }
        return value
    }

    fun putString(key: String,value:String){
        sharedWritable?.putString(key,value)
        sharedWritable?.commit()
    }


    fun getInt(key :String,default:Int):Int{
        var value = default
        sharedReadable?.let {
          value = it.getInt(key,default)
        }
        return value
    }

    fun putInt(key: String,value:Int){
        sharedWritable?.putInt(key,value)
        sharedWritable?.commit()
    }

    fun putBoolean(key: String,value:Boolean){
        sharedWritable?.putBoolean(key,value)
        sharedWritable?.commit()
    }
    fun getBoolean(key: String,default:Boolean):Boolean{
        var value = default
        sharedReadable?.let {
           value =  it.getBoolean(key,default)
        }
        return value
    }
}