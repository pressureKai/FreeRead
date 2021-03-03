/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kai.common.rxhttp.cookie.store

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.kai.common.rxhttp.cookie.SerializableCookie.Companion.decodeCookie
import com.kai.common.rxhttp.cookie.SerializableCookie.Companion.encodeCookie
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/14
 * 描    述：使用 SharedPreferences 持久化存储 cookie
 * 修订历史：
 * ================================================
 */
class SPCookieStore(context: Context) : CookieStore {
    /**
     * 数据结构如下
     * Url.host -> cookieToken1,cookieToken2,cookieToken3
     * cookie_cookieToken1 -> cookie1
     * cookie_cookieToken2 -> cookie2
     * cookie_cookieToken3 -> cookie3
     */
    private val cookies: MutableMap<String, ConcurrentHashMap<String?, Cookie>>
    private val cookiePrefs: SharedPreferences
    private fun getCookieToken(cookie: Cookie?): String {
        return cookie!!.name() + "@" + cookie.domain()
    }

    /** 将url的所有Cookie保存在本地  */
    @Synchronized
    override fun saveCookie(url: HttpUrl?, urlCookies: List<Cookie>?) {
        for (cookie in urlCookies!!) {
            saveCookie(url, cookie)
        }
    }

    @Synchronized
    override fun saveCookie(url: HttpUrl?, cookie: Cookie?) {
        if (!cookies.containsKey(url!!.host())) {
            cookies[url.host()] = ConcurrentHashMap()
        }
        //当前cookie是否过期
        if (isCookieExpired(cookie)) {
            removeCookie(url, cookie)
        } else {
            saveCookie(url, cookie, getCookieToken(cookie))
        }
    }

    /** 保存cookie，并将cookies持久化到本地  */
    private fun saveCookie(url: HttpUrl?, cookie: Cookie?, cookieToken: String) {
        //内存缓存
        cookies[url!!.host()]!![cookieToken] = cookie!!
        //文件缓存
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.putString(url.host(), TextUtils.join(",", cookies[url.host()]!!.keys))
        prefsWriter.putString(
            COOKIE_NAME_PREFIX + cookieToken, encodeCookie(
                url.host(), cookie
            )
        )
        prefsWriter.apply()
    }

    /** 根据当前url获取所有需要的cookie,只返回没有过期的cookie  */
    @Synchronized
    override fun loadCookie(url: HttpUrl?): List<Cookie> {
        val ret: MutableList<Cookie> = ArrayList()
        if (!cookies.containsKey(url!!.host())) return ret
        val urlCookies: Collection<Cookie> = cookies[url.host()]!!.values
        for (cookie in urlCookies) {
            if (isCookieExpired(cookie)) {
                removeCookie(url, cookie)
            } else {
                ret.add(cookie)
            }
        }
        return ret
    }

    /** 根据url移除当前的cookie  */
    @Synchronized
    override fun removeCookie(url: HttpUrl?, cookie: Cookie?): Boolean {
        if (!cookies.containsKey(url!!.host())) {
            return false
        }
        val cookieToken = getCookieToken(cookie)
        if (!cookies[url.host()]!!.containsKey(cookieToken)) {
            return false
        }

        //内存移除
        cookies[url.host()]!!.remove(cookieToken)
        //文件移除
        val prefsWriter = cookiePrefs.edit()
        if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)) {
            prefsWriter.remove(COOKIE_NAME_PREFIX + cookieToken)
        }
        prefsWriter.putString(url.host(), TextUtils.join(",", cookies[url.host()]!!.keys))
        prefsWriter.apply()
        return true
    }

    @Synchronized
    override fun removeCookie(url: HttpUrl?): Boolean {
        if (!cookies.containsKey(url!!.host())) {
            return false
        }

        //内存移除
        val urlCookie = cookies.remove(url.host())!!
        //文件移除
        val cookieTokens: Set<String?> = urlCookie.keys
        val prefsWriter = cookiePrefs.edit()
        for (cookieToken in cookieTokens) {
            if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + cookieToken)
            }
        }
        prefsWriter.remove(url.host())
        prefsWriter.apply()
        return true
    }

    @Synchronized
    override fun removeAllCookie(): Boolean {
        //内存移除
        cookies.clear()
        //文件移除
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()
        return true
    }

    /** 获取所有的cookie  */
    @get:Synchronized
    override val allCookie: List<Cookie>
        get() {
            val ret: MutableList<Cookie> = ArrayList()
            for (key in cookies.keys) {
                ret.addAll(cookies[key]!!.values)
            }
            return ret
        }

    @Synchronized
    override fun getCookie(url: HttpUrl?): List<Cookie> {
        val ret: MutableList<Cookie> = ArrayList()
        val mapCookie: Map<String?, Cookie>? = cookies[url!!.host()]
        if (mapCookie != null) {
            ret.addAll(mapCookie.values)
        }
        return ret
    }

    companion object {
        private const val COOKIE_PREFS = "rx_http_utils_cookie" //cookie使用prefs保存
        private const val COOKIE_NAME_PREFIX = "cookie_" //cookie持久化的统一前缀

        /** 当前cookie是否过期  */
        private fun isCookieExpired(cookie: Cookie?): Boolean {
            return cookie!!.expiresAt() < System.currentTimeMillis()
        }
    }

    init {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE)
        cookies = HashMap()

        //将持久化的cookies缓存到内存中,数据结构为 Map<Url.host, Map<CookieToken, Cookie>>
        val prefsMap = cookiePrefs.all
        for ((key, value) in prefsMap) {
            if (value != null && !key.startsWith(COOKIE_NAME_PREFIX)) {
                //获取url对应的所有cookie的key,用","分割
                val cookieNames = TextUtils.split(value as String?, ",")
                for (name in cookieNames) {
                    //根据对应cookie的Key,从xml中获取cookie的真实值
                    val encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null)
                    if (encodedCookie != null) {
                        val decodedCookie = decodeCookie(encodedCookie)
                        if (decodedCookie != null) {
                            if (!cookies.containsKey(key)) {
                                cookies[key] = ConcurrentHashMap()
                            }
                            cookies[key]!![name] = decodedCookie
                        }
                    }
                }
            }
        }
    }
}