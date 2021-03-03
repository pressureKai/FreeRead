package com.kai.common.rxhttp.factory

import com.kai.common.rxhttp.manager.RxUrlManager
import com.kai.common.rxhttp.retrofit.RetrofitBuilder
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import java.util.*

/**
 * <pre>
 * @author : Allen
 * e-mail  : lygttpod@163.com
 * date    : 2019/03/23
 * desc    :
</pre> *
 */
class ApiFactory private constructor() {
    private lateinit var callAdapterFactory: Array<CallAdapter.Factory>
    private lateinit var converterFactory: Array<Converter.Factory>
    private var okHttpClient: OkHttpClient? = null

    /**
     * 清空所有api缓存（用于切换环境时候使用）
     */
    fun clearAllApi() {
        apiServiceCache.clear()
    }

    fun setCallAdapterFactory(vararg callAdapterFactory: CallAdapter.Factory): ApiFactory {
        this.callAdapterFactory = callAdapterFactory as Array<CallAdapter.Factory>
        return this
    }

    fun setConverterFactory(vararg converterFactory: Converter.Factory): ApiFactory {
        this.converterFactory = converterFactory as Array<Converter.Factory>
        return this
    }

    fun setOkClient(okHttpClient: OkHttpClient?): ApiFactory {
        this.okHttpClient = okHttpClient
        return this
    }

    fun setBaseUrl(baseUrl: String?): ApiFactory {
        RxUrlManager.instance!!.setUrl(baseUrl!!)
        return this
    }

    fun <A> createApi(apiClass: Class<A>?): A? {
        val urlKey = RxUrlManager.DEFAULT_URL_KEY
        val urlValue = RxUrlManager.instance!!.url
        return createApi(urlKey, urlValue, apiClass)
    }

    fun <A> createApi(baseUrlKey: String, baseUrlValue: String?, apiClass: Class<A>?): A? {
        val key = getApiKey(baseUrlKey, apiClass)
        var api = apiServiceCache[key] as A?
        if (api == null) {
            val retrofit = RetrofitBuilder()
                    .setBaseUrl(baseUrlValue)
                    .setCallAdapterFactory(*callAdapterFactory)
                    .setConverterFactory(*converterFactory)
                    .setOkHttpClient(okHttpClient)
                    .build()
            api = retrofit.create(apiClass)
            apiServiceCache[key] = api
        }
        return api
    }

    companion object {
        @Volatile
        var instance: ApiFactory ?=  null
            get() {
                if (field == null) {
                    synchronized(ApiFactory::class.java) {
                        if (field == null) {
                            field = ApiFactory()
                        }
                    }
                }
                return field
            }
        /**
         * 缓存retrofit针对同一个域名下相同的ApiService不会重复创建retrofit对象
         */
        private lateinit var apiServiceCache: HashMap<String, Any?>
        private fun <A> getApiKey(baseUrlKey: String, apiClass: Class<A>?): String {
            return String.format("%s_%s", baseUrlKey, apiClass)
        }
    }

    init {
        apiServiceCache = HashMap()
    }
}