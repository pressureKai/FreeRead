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



    fun getView() : V{
        return iView.get() as V
    }
}