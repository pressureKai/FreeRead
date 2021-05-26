package com.kai.ui.login.interceptor

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.kai.base.application.BaseInit
import com.kai.common.utils.LogUtils.Companion.e
import com.kai.database.CustomDatabase
import com.kai.ui.history.HistoryActivity

@Interceptor(name = "login", priority = 6)
class LoginInterceptorImpl : IInterceptor {
    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        val path = postcard.path
        e("LoginInterceptorImpl", path)
        val userList = CustomDatabase.get().userDao().getUserByOnLine(true)
        val isLogin = userList.isNotEmpty()
        if (isLogin) { // 如果已经登录不拦截
            callback.onContinue(postcard)
        } else {  // 如果没有登录
            when (path) {
                BaseInit.HISTORY -> {
                    val extras = postcard.extras
                    val type = extras.getInt("type")
                    if (type == HistoryActivity.LIKE) {
                        callback.onInterrupt(null)
                    } else {
                        callback.onContinue(postcard)
                    }
                }
                else -> callback.onContinue(postcard)
            }
        }
    }

    override fun init(context: Context) {
        e("LoginInterceptorImpl", "路由登录拦截器初始化成功")
    }
}