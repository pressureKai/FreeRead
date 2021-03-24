package com.kai.base.mvp.base

import java.lang.ref.WeakReference

abstract class BasePresenter<V : IView> :IPresenter<V>{
    lateinit var iView :WeakReference<V>
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