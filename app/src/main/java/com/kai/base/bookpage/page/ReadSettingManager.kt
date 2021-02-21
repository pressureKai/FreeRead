package com.kai.base.bookpage.page

import com.kai.base.utils.SharedPreferenceUtils

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
    val SAHRED_READ_TEXT_SIZE = "shared_read_text_size"
    val SHARED_READ_IS_TEXT_DEFAULT = "shared_read_is_text_default"
    val SHARED_READ_PAGE_MODE = "shared_read_page_mode"
    val SHARED_READ_NIGHT_MODE = "shared_read_night_mode"
    //volume  容量体积
    val SHARED_READ_VOLUME_TRUE_PAGE = "shared_read_volume_true_page"
    val SHARED_READ_FULL_SCREEN = "shared_read_full_screen"
    //convert 转变，转换
    val SHARED_READ_CONVERT_TYPE = "shared_read_convert_type"


    private var sharedPreferenceUtils : SharedPreferenceUtils ?= null




    companion object{
        @Volatile
        private var sInstance: ReadSettingManager? = null
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




    fun setBrightness(progress :Int){

    }
    fun setPageStyle(pageStyle :PageStyle){
        sharedPreferenceUtils?.putInt(SHARED_READ_BG,pageStyle.ordinal)
    }
}