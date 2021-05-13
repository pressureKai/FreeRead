package com.kai.common.utils

import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import com.kai.common.application.BaseApplication

/**
 *
 * @ProjectName:    common
 * @Description:    屏幕宽高，像素相关工具类
 * @Author:         pressureKai
 * @UpdateDate:     2021/2/22 9:47
 */
class ScreenUtils {
    companion object{
        fun dpToPx(dp: Int) :Int{
            var px = 0
            try {
                val displayMetrics = getDisplayMetrics()
                displayMetrics?.let {
                    px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp.toFloat(),
                        it
                    ).toInt()
                }
            }catch (e: Exception){

            }
            return px
        }


        fun pxToDp(px: Int) :Int{
            var dp = 0
            try {
                val displayMetrics = getDisplayMetrics()
                displayMetrics?.let {
                    dp = (px / it.density).toInt()
                }
            }catch (e: Exception){

            }
            return dp
        }


        fun spToPx(sp: Int) :Int{
            var px = 0
            try {
                val displayMetrics = getDisplayMetrics()
                displayMetrics?.let {
                  px = TypedValue.applyDimension(
                      TypedValue.COMPLEX_UNIT_SP,
                      sp.toFloat(),
                      it
                  ).toInt()
                }
            }catch (e: Exception){

            }
            return px
        }


        fun pxToSp(px: Int) :Int{
            var sp = 0
            try {
                val displayMetrics = getDisplayMetrics()
                displayMetrics?.let {
                    sp = (px / it.scaledDensity).toInt()
                }

            }catch (e: Exception) {

            }
            return sp
        }


        fun getAppSize() : IntArray{
            val size = IntArray(2)
            val displayMetrics = getDisplayMetrics()
            try {
                displayMetrics?.let {
                    size[0] = it.widthPixels
                    size[1] = it.heightPixels
                }
            }catch (e:Exception){

            }
            return size
        }



        fun getStatusBarHeight() :Int{
            var statusBarHeight = 0
            try {
                BaseApplication.getContext()?.let {
                    val resources = it.resources
                    val resourceId = resources.getIdentifier("status_bar_height",
                        "dimen",
                        "android")
                    statusBarHeight = resources.getDimensionPixelSize(resourceId)
                }
            }catch (e:Exception){

            }
            return statusBarHeight
        }


        fun getNavigationBarHeight() :Int{
            var navigationBarHeight = 0
            try {
                BaseApplication.getContext()?.let {
                    val resources = it.resources
                    val id = resources.getIdentifier("navigation_bar_height",
                        "dimen",
                        "android")
                    if(id > 0 && hasNavigationBar()){
                        navigationBarHeight = resources.getDimensionPixelSize(id)
                    }
                }
            }catch (e:java.lang.Exception){

            }
            return navigationBarHeight
        }



        private fun hasNavigationBar() :Boolean{
            var hasNavigationBar = false
            BaseApplication.getContext()?.let {
                val resources = it.resources
                val id = resources.getIdentifier("config_showNavigationBar",
                    "bool",
                    "android")

                if(id > 0){
                    hasNavigationBar = resources.getBoolean(id)
                }
                try {
                    val systemPropertiesClass = Class.forName("android.os.SystemProperties")
                    val method = systemPropertiesClass.getMethod("get", String::class.java)
                    val navBarOverride = method.invoke(systemPropertiesClass, "qemu.hw.mainkeys").toString()
                    if(navBarOverride == "1"){
                        hasNavigationBar = false
                    }else if(navBarOverride == "0"){
                        hasNavigationBar = true
                    }
                }catch (e:Exception){

                }
            }


            return hasNavigationBar
        }

        private fun getDisplayMetrics() :DisplayMetrics?{
            var metrics :DisplayMetrics ? = null
            BaseApplication.getContext()?.let {
               metrics = it.resources.displayMetrics
            }
            return metrics
        }
    }
}