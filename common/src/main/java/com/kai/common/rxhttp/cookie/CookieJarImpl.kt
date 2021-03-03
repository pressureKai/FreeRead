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
package com.kai.common.rxhttp.cookie

import com.kai.common.rxhttp.cookie.store.CookieStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/12
 * 描    述：CookieJar的实现类，默认管理了用户自己维护的cookie
 * 修订历史：
 * ================================================
 */
class CookieJarImpl(cookieStore: CookieStore?) : CookieJar {
    val cookieStore: CookieStore
    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.saveCookie(url, cookies)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.loadCookie(url)
    }

    init {
        requireNotNull(cookieStore) { "cookieStore can not be null!" }
        this.cookieStore = cookieStore
    }
}