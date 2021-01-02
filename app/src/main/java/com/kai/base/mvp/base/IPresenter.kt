package com.kai.base.mvp.base


/**
 * 桥梁接口（含model功能）连接View与数据源
 */
interface IPresenter <in V>{
    fun register(view :V)
    fun unRegister()
}