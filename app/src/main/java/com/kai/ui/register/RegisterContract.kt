package com.kai.ui.register

import com.kai.base.mvp.base.IView
import com.kai.entity.User
import io.reactivex.rxjava3.core.Observable

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-注册
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class RegisterContract {
    interface View :IView{
        fun onRegister(code: Int,user: User?)
    }

    interface Presenter{
        fun register(account: String,password: String,question: String,answer: String)
    }
}