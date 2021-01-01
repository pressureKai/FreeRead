package com.kai.base.rxhttp.interceptor

import com.kai.base.rxhttp.utils.NetUtils.isNetworkConnected
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * <pre>
 * @author : Allen
 * date    : 2018/06/14
 * desc    : 网络缓存 参考 https://www.jianshu.com/p/cf59500990c7
 * version : 1.0
</pre> *
 */
class NetCacheInterceptor(
    /**
     * 默认缓存60秒
     */
    private val cacheTime: Int
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val connected = isNetworkConnected
        if (connected) {
            //如果有网络，缓存60s
            val response = chain.proceed(request)
            val builder = CacheControl.Builder()
                .maxAge(cacheTime, TimeUnit.SECONDS)
            return response.newBuilder()
                .header("Cache-Control", builder.build().toString())
                .removeHeader("Pragma")
                .build()
        }
        //如果没有网络，不做处理，直接返回
        return chain.proceed(request)
    }
}