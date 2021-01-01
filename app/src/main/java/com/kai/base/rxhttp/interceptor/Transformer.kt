package com.kai.base.rxhttp.interceptor

import com.kai.base.rxhttp.interfaces.ILoadingView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Created by Allen on 2016/12/20.
 *
 *
 *
 * @author Allen
 * 控制操作线程的辅助类
 */
object Transformer {
    /**
     * 无参数
     *
     * @param <T> 泛型
     * @return 返回Observable
    </T> */
    fun <T> switchSchedulers(): ObservableTransformer<T, T> {
        return switchSchedulers(null)
    }

    /**
     * 带参数  显示loading对话框
     *
     * @param loadingView loading
     * @param <T>         泛型
     * @return 返回Observable
    </T> */
    private fun <T> switchSchedulers(loadingView: ILoadingView?): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe { loadingView?.showLoadingView() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { loadingView?.hideLoadingView() }
        }
    }
}