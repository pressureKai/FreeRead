package com.kai.common.utils

import java.io.Closeable
import java.lang.Exception

/**
 *
 * @ProjectName:    CommonApplication
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/8 10:17
 */
class IOUtils {
    companion object {
        fun close(closeable: Closeable?) {
            if(closeable == null){
                return
            }

            try {
                closeable.close()
            }catch (e: Exception){
                e.printStackTrace()
                LogUtils.e("IOUtils",e.toString())
            }
        }
    }
}