package com.kai.common.rxhttp.retrofit

import com.kai.common.rxhttp.gson.GsonAdapter
import com.kai.common.rxhttp.http.SSLUtils.sslSocketFactory
import com.kai.common.rxhttp.interceptor.RxHttpLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * <pre>
 * @author : Allen
 * e-mail  : lygttpod@163.com
 * date    : 2019/03/23
 * desc    :
</pre> *
 */
class RetrofitBuilder {
    private var baseUrl: String? = null
    private var callAdapterFactory: Array<out CallAdapter.Factory>? = null
    private var converterFactory: Array<out Converter.Factory>? = null
    private var okHttpClient: OkHttpClient? = null
    fun setBaseUrl(baseUrl: String?): RetrofitBuilder {
        this.baseUrl = baseUrl
        return this
    }

    fun setCallAdapterFactory(vararg callAdapterFactory: CallAdapter.Factory): RetrofitBuilder {
        this.callAdapterFactory = callAdapterFactory
        return this
    }

    fun setConverterFactory(vararg converterFactory: Converter.Factory): RetrofitBuilder {
        this.converterFactory = converterFactory
        return this
    }

    fun setOkHttpClient(okHttpClient: OkHttpClient?): RetrofitBuilder {
        this.okHttpClient = okHttpClient
        return this
    }

    fun build(): Retrofit {
        val builder = Retrofit.Builder()
        builder.baseUrl(baseUrl)
        if (callAdapterFactory == null || callAdapterFactory!!.isEmpty()) {
            builder.addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        } else {
            for (factory in callAdapterFactory!!) {
                builder.addCallAdapterFactory(factory)
            }
        }
        if (converterFactory == null || converterFactory!!.isEmpty()) {
            builder.addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonAdapter.buildGson()))
        } else {
            for (factory in converterFactory!!) {
                builder.addConverterFactory(factory)
            }
        }
        if (okHttpClient == null) {
            builder.client(createOkHttpClient())
        } else {
            builder.client(okHttpClient)
        }
        return builder.build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.readTimeout(10, TimeUnit.SECONDS)
        builder.writeTimeout(10, TimeUnit.SECONDS)
        builder.connectTimeout(10, TimeUnit.SECONDS)
        val sslParams = sslSocketFactory
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
        val loggingInterceptor = HttpLoggingInterceptor(RxHttpLogger())
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)
        return builder.build()
    }
}