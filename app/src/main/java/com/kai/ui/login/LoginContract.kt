package com.kai.ui.login

import com.kai.base.mvp.base.IView
import com.kai.common.eventBusEntity.BaseEntity
import com.kai.entity.User

/**
 *
 * @ProjectName:    APP-bookPage
 * @Description:    契约类-约束MVP中的接口
 * @Author:         pressureKai
 * @UpdateDate:     2021/3/24 10:42
 */
class LoginContract {
    interface View :IView{
        fun onLogin(user: BaseEntity<User>)
    }

    interface Presenter{
        fun login(account: String,password: String)
    }
}