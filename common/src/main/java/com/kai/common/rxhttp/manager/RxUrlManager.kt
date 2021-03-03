package com.kai.common.rxhttp.manager

import com.kai.common.rxhttp.RxHttpUtils
import com.kai.common.rxhttp.factory.ApiFactory
import java.util.*

/**
 * <pre>
 * @author : Allen
 * e-mail  : lygttpod@163.com
 * date    : 2019/03/23
 * desc    : 多域名管理类
</pre> *
 */
class RxUrlManager private constructor() {
    private var urlMap: MutableMap<String, String>

    /**
     * 一次性传入urlMap
     *
     * @param urlMap map
     * @return RxUrlManager
     */
    fun setMultipleUrl(urlMap: MutableMap<String, String>): RxUrlManager {
        this.urlMap = urlMap
        return this
    }

    /**
     * 向map中添加url
     *
     * @param urlKey   key
     * @param urlValue value
     * @return RxUrlManager
     */
    fun addUrl(urlKey: String, urlValue: String): RxUrlManager {
        urlMap[urlKey] = urlValue
        return this
    }

    /**
     * 从map中删除某个url
     *
     * @param urlKey 需要删除的urlKey
     * @return RxUrlManager
     */
    fun removeUrlByKey(urlKey: String): RxUrlManager {
        urlMap.remove(urlKey)
        return this
    }

    /**
     * 针对单个baseUrl切换的时候清空老baseUrl的所有信息
     *
     * @param urlValue url
     * @return RxUrlManager
     */
    fun setUrl(urlValue: String): RxUrlManager {
        urlMap[DEFAULT_URL_KEY] = urlValue
        return this
    }

    /**
     * 获取全局唯一的baseUrl
     *
     * @return url
     */
    val url: String?
        get() = getUrlByKey(DEFAULT_URL_KEY)

    /**
     * 根据key
     *
     * @param urlKey 获取对应的url
     * @return url
     */
    fun getUrlByKey(urlKey: String): String? {
        return urlMap[urlKey]
    }

    /**
     * 清空设置的url相关的所以信息
     * 相当于重置url
     * 动态切换生产测试环境时候调用
     *
     * @return RxUrlManager
     */
    fun clear(): RxUrlManager {
        urlMap.clear()
        ApiFactory.instance?.clearAllApi()
        RxHttpUtils.removeAllCookie()
        return this
    }

    companion object {
        @JvmStatic
        @Volatile
        var instance: RxUrlManager? = null
            get() {
                if (field == null) {
                    synchronized(RxUrlManager::class.java) {
                        if (field == null) {
                            field = RxUrlManager()
                        }
                    }
                }
                return field
            }
            private set
        @JvmField
        var DEFAULT_URL_KEY = "rx_default_url_key"
    }

    init {
        urlMap = HashMap()
    }
}