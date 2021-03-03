package com.kai.common.rxhttp.manager

import io.reactivex.rxjava3.disposables.Disposable


/**
 * <pre>
 * @author : xiaoyao
 * e-mail  : xiaoyao@51vest.com
 * date    : 2018/08/09
 * desc    : 请求管理接口
 * version : 1.0
</pre> *
 */
interface IRxHttpManager<T> {
    /**
     * 添加
     *
     * @param tag        tag
     * @param disposable disposable
     */
    fun add(tag: T, disposable: Disposable?)

    /**
     * 移除请求
     *
     * @param tag tag
     */
    fun remove(tag: T)

    /**
     * 取消某个tag的请求
     *
     * @param tag tag
     */
    fun cancel(tag: T)

    /**
     * 取消某些tag的请求
     * vararg  多个请求参数
     * @param tags tags
     */
    fun cancel(vararg tags: T)

    /**
     * 取消所有请求
     */
    fun cancelAll()
}