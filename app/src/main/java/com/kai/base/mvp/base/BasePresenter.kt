package com.kai.base.mvp.base

import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * des 协调，主持者 (分层思想) 被View层所引用
 */
abstract class BasePresenter<V : IView> :IPresenter<V>{
    lateinit var iView :WeakReference<V>
    override fun register(view: V) {
        iView = WeakReference(view)
    }


    override fun unRegister() {
        iView.clear()
    }


    fun getView() : V?{
        return  try {
            iView.get() as V
        }catch (e :Exception){
            null
        }
    }
}