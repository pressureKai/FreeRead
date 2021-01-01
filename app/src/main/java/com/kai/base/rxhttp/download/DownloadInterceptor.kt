package com.kai.base.rxhttp.download

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * <pre>
 * @author : Allen
 * e-mail  : lygttpod@163.com
 * date    : 2019/04/09
 * desc    : 来取消Gzip压缩，Content-Length便是正常数据,否则有的接口通过Gzip压缩Content-Length返回为-1
</pre> *
 */
class DownloadInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(
            request.newBuilder()
                .addHeader("Accept-Encoding", "identity")
                .build()
        )
    }
}