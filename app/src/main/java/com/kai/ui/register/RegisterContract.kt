package com.kai.ui.register

import com.kai.base.mvp.base.IView

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-注册
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class RegisterContract {
    interface View :IView{
        fun onRegister(code: Int)
    }

    interface Presenter{
        fun register(account: String,password: String,question: String,answer: String)
    }
}