package com.kai.common.rxhttp.manager

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

/**
 * <pre>
 * @author : xiaoyao
 * e-mail  : xiaoyao@51vest.com
 * date    : 2018/08/09
 * desc    : 管理请求--用于取消请求时候使用
 * version : 1.0
</pre> *
 */
class RxHttpManager private constructor() : IRxHttpManager<Any> {
    private val mMaps: HashMap<Any, CompositeDisposable?> = HashMap()
    override fun add(tag: Any, disposable: Disposable?) {
        if (null == tag) {
            return
        }
        //tag下的一组或一个请求，用来处理一个页面的所以请求或者某个请求
        //设置一个相同的tag就行就可以取消当前页面所有请求或者某个请求了
        val compositeDisposable = mMaps[tag]
        if (compositeDisposable == null) {
            val compositeDisposableNew = CompositeDisposable()
            compositeDisposableNew.add(disposable!!)
            mMaps[tag] = compositeDisposableNew
        } else {
            compositeDisposable.add(disposable!!)
        }
    }

    override fun remove(tag: Any) {
        if (null == tag) {
            return
        }
        if (!mMaps.isEmpty()) {
            mMaps.remove(tag)
        }
    }

    override fun cancel(tag: Any) {
        if (null == tag) {
            return
        }
        if (mMaps.isEmpty()) {
            return
        }
        if (null == mMaps[tag]) {
            return
        }
        if (!mMaps[tag]!!.isDisposed) {
            mMaps[tag]!!.dispose()
            mMaps.remove(tag)
        }
    }

    override fun cancel(vararg tags: Any) {
        if (null == tags) {
            return
        }
        for (tag in tags) {
            cancel(tag)
        }
    }

    override fun cancelAll() {
        if (mMaps.isEmpty()) {
            return
        }
        val it: MutableIterator<Map.Entry<Any, CompositeDisposable?>> = mMaps.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val disposable = entry.value
            //如果直接使用map的remove方法会报这个错误java.util.ConcurrentModificationException
            //所以要使用迭代器的方法remove
            if (null != disposable) {
                if (!disposable.isDisposed) {
                    disposable.dispose()
                    it.remove()
                }
            }
        }
    }

    companion object {
        private var mInstance: RxHttpManager? = null
        fun get(): RxHttpManager? {
            if (mInstance == null) {
                synchronized(RxHttpManager::class.java) {
                    if (mInstance == null) {
                        mInstance = RxHttpManager()
                    }
                }
            }
            return mInstance
        }
    }

}