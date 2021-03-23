package com.kai.base.mvp.base


/**
 * des 桥梁接口
 * 1. 调用Model层获取数据
 * 2. 将数据返回给View
 */
interface IPresenter <in V>{
    fun register(view :V)
    fun unRegister()
}