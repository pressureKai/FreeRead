package com.kai.base.application

/**
 *
 */
class PageConfig {
    companion object{
        //module  初始化类的路径名称
        private const val initPath :String = "com.kai.base.application.BaseInit"
        //路径集合
        val initModules  = arrayListOf<String>(initPath)
    }
}