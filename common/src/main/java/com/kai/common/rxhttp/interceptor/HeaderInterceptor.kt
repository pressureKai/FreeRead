package com.kai.common.rxhttp.interceptor

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Created by Allen on 2017/5/3.
 *
 *
 *
 * @author Allen
 * 请求拦截器  统一添加请求头使用
 */
abstract class HeaderInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headers = buildHeaders()
        return if (headers == null || headers.isEmpty()) {
            chain.proceed(request)
        } else {
            chain.proceed(
                request.newBuilder()
                    .headers(buildHeaders(request, headers))
                    .build()
            )
        }
    }

    private fun buildHeaders(request: Request, headerMap: Map<String, String>): Headers? {
        val headers = request.headers()
        return if (headers != null) {
            val builder = headers.newBuilder()
            for (key in headerMap.keys) {
                builder.add(key, headerMap[key])
            }
            builder.build()
        } else {
            headers
        }
    }

    abstract fun buildHeaders(): Map<String, String>?
}