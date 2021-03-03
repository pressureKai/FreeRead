package com.kai.common.rxhttp.interceptor

import android.util.Log
import com.kai.common.rxhttp.utils.JsonUtil.formatJson
import okhttp3.logging.HttpLoggingInterceptor

/**
 * <pre>
 * @author : Allen
 * date    : 2018/06/14
 * desc    : 日志打印格式化处理  https://www.jianshu.com/p/e044cab4f530
 * version : 1.0
</pre> *
 */
class RxHttpLogger : HttpLoggingInterceptor.Logger {
    private val mMessage = StringBuffer()
    override fun log(message: String) {
        // 请求或者响应开始
        var messageInside = message
        if (messageInside.startsWith("--> POST")) {
            mMessage.setLength(0)
            mMessage.append(" ")
            mMessage.append("\r\n")
        }
        if (messageInside.startsWith("--> GET")) {
            mMessage.setLength(0)
            mMessage.append(" ")
            mMessage.append("\r\n")
        }
        // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
        if (messageInside.startsWith("{") && messageInside.endsWith("}")
            || messageInside.startsWith("[") && messageInside.endsWith("]")
        ) {
            messageInside = formatJson(messageInside)
        }
        mMessage.append(
            """
         $messageInside
         
         """.trimIndent()
        )
        // 请求或者响应结束，打印整条日志
        if (messageInside.startsWith("<-- END HTTP")) {
            Log.e("RxHttpUtils", mMessage.toString())
        }
    }
}