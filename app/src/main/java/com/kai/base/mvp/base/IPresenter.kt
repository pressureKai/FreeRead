package com.kai.base.mvp.base


/**
 * des 桥梁接口
 * 1. 调用Model层获取数据
 * 2. 将数据返回给View
 * 3. 并不需要所在View所实现的细节
 */
interface IPresenter<in V : IView>{
    fun register(view :V)
    fun unRegister()
}