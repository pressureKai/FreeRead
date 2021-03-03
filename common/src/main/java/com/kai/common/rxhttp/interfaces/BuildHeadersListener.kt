package com.kai.common.rxhttp.interfaces

/**
 * <pre>
 * @author : Allen
 * e-mail  : lygttpod@163.com
 * date    : 2019/03/03
 * desc    : 请求头interface
</pre> *
 */
interface BuildHeadersListener {
    fun buildHeaders(): Map<String, String>
}