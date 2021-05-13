package com.kai.bookpage.page

import com.kai.common.utils.ScreenUtils
import com.kai.common.utils.SharedPreferenceUtils


class ReadSettingManager {


    val READ_BG_DEFAULT = 0
    val READ_BG_1 = 1
    val READ_BG_2 = 2
    val READ_BG_3 = 3
    val READ_BG_4 = 4
    val NIGHT_MODE = 5

    val SHARED_READ_BG = "shared_read_bg"
    val SHARED_READ_BRIGHTNESS = "shared_brightness"
    val SHARED_READ_IS_BRIGHTNESS_AUTO = "shared_read_is_brightness_auto"
    val SHARED_READ_TEXT_SIZE = "shared_read_text_size"
    val SHARED_READ_IS_TEXT_DEFAULT = "shared_read_is_text_default"
    val SHARED_READ_PAGE_MODE = "shared_read_page_mode"
    val SHARED_READ_NIGHT_MODE = "shared_read_night_mode"
    //volume  容量体积
    val SHARED_READ_VOLUME_TRUE_PAGE = "shared_read_volume_true_page"
    val SHARED_READ_FULL_SCREEN = "shared_read_full_screen"
    //convert 转变，转换

    private var sharedPreferenceUtils : SharedPreferenceUtils ?= null


    companion object{
        const val defaultTextSize = 28
        @Volatile
        private var sInstance: ReadSettingManager? = null
        val SHARED_READ_CONVERT_TYPE = "shared_read_convert_type"
        fun getInstance() :ReadSettingManager?{
            if(sInstance == null){
                synchronized(ReadSettingManager::class.java){
                    if(sInstance == null){
                        sInstance = ReadSettingManager()
                    }
                }
            }
            return sInstance
        }
    }


    private constructor(){
        sharedPreferenceUtils = SharedPreferenceUtils.getInstance()
    }

    /**
     * 改变页面的亮度
     */
    fun setBrightness(progress: Int){
         sharedPreferenceUtils?.putInt(SHARED_READ_BRIGHTNESS, progress)
    }

    /**
     * 获取页面亮度
     */
    fun getBrightness():Int{
        var brightness = 40
        sharedPreferenceUtils?.let {
            brightness = it.getInt(SHARED_READ_BRIGHTNESS, 40)
        }
        return brightness
    }


    // ordinal 序列,下标
    fun setPageStyle(pageStyle: PageStyle){
        sharedPreferenceUtils?.putInt(SHARED_READ_BG, pageStyle.ordinal)
    }


    fun getPageStyle() :PageStyle{
        var styleIndex = 0
        sharedPreferenceUtils?.let {
            styleIndex = it.getInt(SHARED_READ_BG, PageStyle.BG_0.ordinal)
        }
        return PageStyle.values()[styleIndex]
    }

    fun setAutoBrightness(isAuto: Boolean){
        sharedPreferenceUtils?.putBoolean(SHARED_READ_IS_BRIGHTNESS_AUTO, isAuto)
    }


    fun isBrightnessAuto() :Boolean{
        var isBrightness = false
        sharedPreferenceUtils?.let {
            it.getBoolean(SHARED_READ_IS_BRIGHTNESS_AUTO, false)
        }
        return isBrightness
    }

    fun setDefaultTextSize(isDefault: Boolean){
        sharedPreferenceUtils?.putBoolean(SHARED_READ_IS_TEXT_DEFAULT, isDefault)
    }

    fun isDefaultTextSize() :Boolean{
        var isDefault = false
        sharedPreferenceUtils?.let {
            isDefault = it.getBoolean(SHARED_READ_IS_TEXT_DEFAULT, false)
        }
        return isDefault
    }

    fun setTextSize(textSize: Int){
        sharedPreferenceUtils?.putInt(SHARED_READ_TEXT_SIZE, textSize)
    }

    fun getTextSize() :Int{
        var textSize = 0
        sharedPreferenceUtils?.let {
           textSize = it.getInt(SHARED_READ_TEXT_SIZE, ScreenUtils.spToPx(defaultTextSize))
        }
        return textSize
    }

    fun setPageMode(mode: PageMode){
        sharedPreferenceUtils?.putInt(SHARED_READ_PAGE_MODE, mode.ordinal)
    }

    fun getPageMode() :PageMode{
        var modeIndex = 0
        sharedPreferenceUtils?.let {
            modeIndex = it.getInt(SHARED_READ_PAGE_MODE, PageMode.SIMULATION.ordinal)
        }
        return PageMode.values()[modeIndex]
    }


    fun setNightMode(isNight: Boolean){
        sharedPreferenceUtils?.putBoolean(SHARED_READ_NIGHT_MODE, isNight)
    }

    fun isNightMode() :Boolean{
        var isNightMode = false
        sharedPreferenceUtils?.let {
            isNightMode = it.getBoolean(SHARED_READ_NIGHT_MODE, false)
        }
        return isNightMode
    }


    fun setVolumeTurnPage(isTurn: Boolean){
        sharedPreferenceUtils?.putBoolean(SHARED_READ_VOLUME_TRUE_PAGE, isTurn)
    }

    fun isVolumeTurnPage() :Boolean{
        var isTurn = false
        sharedPreferenceUtils?.let {
            isTurn = it.getBoolean(SHARED_READ_VOLUME_TRUE_PAGE, false)
        }
        return isTurn
    }

    fun setFullScreen(isFullScreen: Boolean){
        sharedPreferenceUtils?.putBoolean(SHARED_READ_FULL_SCREEN, isFullScreen)
    }

    fun isFullScreen() :Boolean{
        var isFullScreen = false
        sharedPreferenceUtils?.let {
            isFullScreen = it.getBoolean(SHARED_READ_FULL_SCREEN, false)
        }
        return isFullScreen
    }


    fun setCovertType(covertType: Int){
        sharedPreferenceUtils?.putInt(SHARED_READ_CONVERT_TYPE, covertType)
    }


    fun getCovertType() :Int{
        var covertType = 0
        sharedPreferenceUtils?.let {
           covertType =  it.getInt(SHARED_READ_CONVERT_TYPE, 0)
        }
        return covertType
    }
}