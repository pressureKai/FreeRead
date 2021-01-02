package com.kai.base.mvp.base

import java.lang.ref.WeakReference

abstract class BasePresenter<V> :IPresenter<V>{
    lateinit var iView :WeakReference<in V>
    override fun register(view: V) {
        iView = WeakReference(view)
    }


    override fun unRegister() {
        iView.clear()
    }

    abstract fun getView() : V
}