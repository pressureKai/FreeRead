package com.kai.base.utils

import android.util.Log
import com.kai.base.application.Constant
import java.lang.Exception


class LogUtils {
    companion object{
        private const val splitCount = 3999
        fun e(messageTag :String,content :String){
            dealContent(0,messageTag,content)
        }




        fun d(messageTag :String,content :String){
            dealContent(2,messageTag,content)
        }



        fun w(messageTag :String,content :String){
            dealContent(1,messageTag,content)
        }



       private fun dealContent(type :Int,messageTag :String,content: String){
            if(!Constant.isDebug){
                return
            }
            if(content.length <= splitCount ){
                printLog(type,messageTag,content)
                return
            }


            try {
                var count = 0
                while (count + splitCount < content.length){
                    var splitString = ""
                    if(count + splitCount < content.length ){
                        splitString = content.substring(count,count.plus(splitCount))
                    }
                    count += splitCount
                    printLog(type,messageTag,splitString)
                }


                if(count < content.length){
                    printLog(type,messageTag,content.substring(count,content.length))
                }
            }catch (e:Exception){
                Log.e(messageTag,"logcat error")
            }

        }



        private fun printLog(type :Int,messageTag: String,content: String){
            when(type){
                0->{
                    Log.e(messageTag,content)
                }
                1->{
                    Log.w(messageTag,content)
                }
                2->{
                    Log.d(messageTag,content)
                }
            }
        }

    }

}