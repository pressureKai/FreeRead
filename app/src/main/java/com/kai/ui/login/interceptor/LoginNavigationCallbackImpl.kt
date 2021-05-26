package com.kai.ui.login.interceptor

import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.kai.base.application.BaseInit

class LoginNavigationCallbackImpl : NavigationCallback {
    //找到了
    override fun onFound(postcard: Postcard) {}

    //找不到了
    override fun onLost(postcard: Postcard) {}

    //跳转成功了
    override fun onArrival(postcard: Postcard) {}
    override fun onInterrupt(postcard: Postcard) {
        val path = postcard.path
        val bundle = postcard.extras
        // 被登录拦截了下来了 
        // 需要调转到登录页面，把参数跟被登录拦截下来的路径传递给登录页面，登录成功后再进行跳转被拦截的页面
        ARouter.getInstance().build(BaseInit.LOGIN)
            .with(bundle)
            .withString(BaseInit.REBACK, path)
            .navigation()
    }
}