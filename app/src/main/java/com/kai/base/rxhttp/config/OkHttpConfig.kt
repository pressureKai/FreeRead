package com.kai.base.rxhttp.config

import android.content.Context
import android.text.TextUtils
import com.kai.base.rxhttp.cookie.CookieJarImpl
import com.kai.base.rxhttp.cookie.store.CookieStore
import com.kai.base.rxhttp.http.SSLUtils
import com.kai.base.rxhttp.http.SSLUtils.SSLParams
import com.kai.base.rxhttp.http.SSLUtils.getSslSocketFactory
import com.kai.base.rxhttp.http.SSLUtils.sslSocketFactory
import com.kai.base.rxhttp.interceptor.HeaderInterceptor
import com.kai.base.rxhttp.interceptor.NetCacheInterceptor
import com.kai.base.rxhttp.interceptor.NoNetCacheInterceptor
import com.kai.base.rxhttp.interceptor.RxHttpLogger
import com.kai.base.rxhttp.interfaces.BuildHeadersListener
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

/**
 * <pre>
 * @author : Allen
 * e-mail  : lygttpod@163.com
 * date    : 2018/05/29
 * desc    : 统一OkHttp配置信息
 * version : 1.0
</pre> *
 */
class OkHttpConfig {
    val okHttpClient: OkHttpClient?
        get() = if (Companion.okHttpClient == null) {
            okHttpClientBuilder.build()
        } else {
            Companion.okHttpClient
        }

    class Builder(var context: Context) {
        private var isDebug = false
        private var isCache = false
        private var cacheTime = 60
        private var noNetCacheTime = 10
        private var cachePath: String? = null
        private var cacheMaxSize: Long = 0
        private var cookieStore: CookieStore? = null
        private var readTimeout: Long = 0
        private var writeTimeout: Long = 0
        private var connectTimeout: Long = 0
        private var bksFile: InputStream? = null
        private var password: String? = null
        private var certificates: Array<out InputStream> ?= null
        private var interceptors: Array<out Interceptor> ?= null
        private var buildHeadersListener: BuildHeadersListener? = null
        private var hostnameVerifier: HostnameVerifier? = null
        fun setHeaders(buildHeadersListener: BuildHeadersListener?): Builder {
            this.buildHeadersListener = buildHeadersListener
            return this
        }

        fun setDebug(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }

        fun setCache(isCache: Boolean): Builder {
            this.isCache = isCache
            return this
        }

        fun setHasNetCacheTime(cacheTime: Int): Builder {
            this.cacheTime = cacheTime
            return this
        }

        fun setNoNetCacheTime(noNetCacheTime: Int): Builder {
            this.noNetCacheTime = noNetCacheTime
            return this
        }

        fun setCachePath(cachePath: String?): Builder {
            this.cachePath = cachePath
            return this
        }

        fun setCacheMaxSize(cacheMaxSize: Long): Builder {
            this.cacheMaxSize = cacheMaxSize
            return this
        }

        fun setCookieType(cookieStore: CookieStore?): Builder {
            this.cookieStore = cookieStore
            return this
        }

        fun setReadTimeout(readTimeout: Long): Builder {
            this.readTimeout = readTimeout
            return this
        }

        fun setWriteTimeout(writeTimeout: Long): Builder {
            this.writeTimeout = writeTimeout
            return this
        }

        fun setConnectTimeout(connectTimeout: Long): Builder {
            this.connectTimeout = connectTimeout
            return this
        }

        fun setAddInterceptor(vararg interceptors: Interceptor): Builder {
            this.interceptors = interceptors
            return this
        }

        fun setSslSocketFactory(vararg certificates: InputStream): Builder {
            this.certificates = certificates
            return this
        }

        fun setSslSocketFactory(
            bksFile: InputStream?,
            password: String?,
            vararg certificates: InputStream
        ): Builder {
            this.bksFile = bksFile
            this.password = password
            this.certificates = certificates
            return this
        }

        fun setHostnameVerifier(hostnameVerifier: HostnameVerifier?): Builder {
            this.hostnameVerifier = hostnameVerifier
            return this
        }

        fun build(): OkHttpClient? {
            instance
            setCookieConfig()
            setCacheConfig()
            setHeadersConfig()
            setSslConfig()
            setHostnameVerifier()
            addInterceptors()
            setTimeout()
            setDebugConfig()
            okHttpClient = okHttpClientBuilder.build()
            return okHttpClient
        }

        private fun addInterceptors() {
            if (null != interceptors) {
                for (interceptor in interceptors!!) {
                    okHttpClientBuilder.addInterceptor(interceptor)
                }
            }
        }

        /**
         * 配置开发环境
         */
        private fun setDebugConfig() {
            if (isDebug) {
                val logInterceptor = HttpLoggingInterceptor(RxHttpLogger())
                logInterceptor.level = HttpLoggingInterceptor.Level.BODY
                okHttpClientBuilder.addInterceptor(logInterceptor)
            }
        }

        /**
         * 配置headers
         */
        private fun setHeadersConfig() {
            if (buildHeadersListener != null) {
                okHttpClientBuilder.addInterceptor(object : HeaderInterceptor() {
                    override fun buildHeaders(): Map<String, String> {
                        return buildHeadersListener!!.buildHeaders()
                    }
                })
            }
        }

        /**
         * 配饰cookie保存到sp文件中
         */
        private fun setCookieConfig() {
            if (null != cookieStore) {
                okHttpClientBuilder.cookieJar(CookieJarImpl(cookieStore))
            }
        }

        /**
         * 配置缓存
         */
        private fun setCacheConfig() {
            val externalCacheDir = context.externalCacheDir ?: return
            defaultCachePath = externalCacheDir.path + "/RxHttpCacheData"
            if (isCache) {
                val cache: Cache = if (!TextUtils.isEmpty(cachePath) && cacheMaxSize > 0) {
                    Cache(File(cachePath!!), cacheMaxSize)
                } else {
                    Cache(
                        File(defaultCachePath!!),
                        defaultCacheSize
                    )
                }
                okHttpClientBuilder
                    .cache(cache)
                    .addInterceptor(NoNetCacheInterceptor(noNetCacheTime))
                    .addNetworkInterceptor(NetCacheInterceptor(cacheTime))
            }
        }

        /**
         * 配置超时信息
         */
        private fun setTimeout() {
            okHttpClientBuilder.readTimeout(
                if (readTimeout == 0L) defaultTimeout else readTimeout,
                TimeUnit.SECONDS
            )
            okHttpClientBuilder.writeTimeout(
                if (writeTimeout == 0L) defaultTimeout else writeTimeout,
                TimeUnit.SECONDS
            )
            okHttpClientBuilder.connectTimeout(
                if (connectTimeout == 0L) defaultTimeout else connectTimeout,
                TimeUnit.SECONDS
            )
            okHttpClientBuilder.retryOnConnectionFailure(true)
        }

        /**
         * 配置证书
         */
        private fun setSslConfig() {
            var sslParams: SSLParams = if (null == certificates) {
                //信任所有证书,不安全有风险
                sslSocketFactory
            } else {
                if (null != bksFile && !TextUtils.isEmpty(password)) {
                    //使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
                    getSslSocketFactory(bksFile, password, *certificates!!)
                } else {
                    //使用预埋证书，校验服务端证书（自签名证书）
                    getSslSocketFactory(*certificates!!)
                }
            }
            okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
        }

        private fun setHostnameVerifier() {
            if (null == hostnameVerifier) {
                okHttpClientBuilder.hostnameVerifier(SSLUtils.UnSafeHostnameVerifier)
            } else {
                okHttpClientBuilder.hostnameVerifier(hostnameVerifier!!)
            }
        }
    }

    companion object {
        private var defaultCachePath: String? = null
        private const val defaultCacheSize = (1024 * 1024 * 100).toLong()
        private const val defaultTimeout: Long = 10
        var instance: OkHttpConfig? = null
            get() {
                if (field == null) {
                    synchronized(OkHttpConfig::class.java) {
                        if (field == null) {
                            field = OkHttpConfig()
                        }
                    }
                }
                return field
            }
            private set
        private lateinit var okHttpClientBuilder: OkHttpClient.Builder
        private var okHttpClient: OkHttpClient? = null
    }

    init {
        okHttpClientBuilder = OkHttpClient.Builder()
    }
}