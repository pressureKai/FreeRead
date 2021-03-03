package com.kai.common.application

/**
 *  子Module Application 的初始化工作
 */
class ModuleConfig {
    companion object{
        //module  初始化类的路径名称
        private const val initPath :String = "com.kai.base.application.BaseInit"
        //路径集合
        val initModules  = arrayListOf<String>(initPath)
    }
}