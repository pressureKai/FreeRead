package com.kai.common.rxhttp.base

import com.kai.common.rxhttp.exception.ApiException
import com.kai.common.rxhttp.interfaces.ISubscriber
import com.kai.common.rxhttp.manager.RxHttpManager
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Created by Allen on 2017/5/3.
 *
 * @author Allen
 *
 *
 * 基类BaseObserver
 */
abstract class BaseObserver<T> : Observer<T>, ISubscriber<T> {
    /**
     * 是否隐藏toast
     *
     * @return
     */
    protected val isHideToast: Boolean
        get() = false

    /**
     * 标记网络请求的tag
     * tag下的一组或一个请求，用来处理一个页面的所以请求或者某个请求
     * 设置一个tag就行就可以取消当前页面所有请求或者某个请求了
     * @return string
     */
    protected fun setTag(): String? {
        return null
    }

    override fun onSubscribe(d: Disposable) {
        setTag()?.let { RxHttpManager.get()?.add(it, d) };
        doOnSubscribe(d)
    }

    override fun onNext(t: T) {
        doOnNext(t)
    }

    override fun onError(e: Throwable) {
        val error = ApiException.handleException(e).message
        setError(error)
    }

    override fun onComplete() {
        doOnCompleted()
    }

    private fun setError(errorMsg: String) {
        doOnError(errorMsg)
    }
}